package com.zia.payments.coupon.service;

import com.zia.payments.coupon.domain.Coupon;
import com.zia.payments.coupon.domain.UserCoupon;
import com.zia.payments.coupon.domain.UserCouponStatus;
import com.zia.payments.coupon.dto.request.ClaimCouponRequest;
import com.zia.payments.coupon.dto.response.ClaimCouponResponse;
import com.zia.payments.coupon.dto.response.IssueCouponResponse;
import com.zia.payments.coupon.repository.CouponRepository;
import com.zia.payments.coupon.repository.UserCouponRepository;
import com.zia.payments.global.exception.ApiException;
import com.zia.payments.global.exception.ErrorCode;
import com.zia.payments.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;

    /**
     * 선착순 쿠폰 발급
     * - 쿠폰 존재/만료 확인
     * - 사전 중복 체크
     * - 선착순이면 issued_count 증가 (rowCount 체크)
     * - user_coupons INSERT (UNIQUE 최종 보장)
     */

    @Override
    public ClaimCouponResponse claim(Long userId, ClaimCouponRequest request, String requestId) {
        if (request == null || request.getCouponCode() == null || request.getCouponCode().isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "couponCode가 필요합니다.");
        }

        Coupon coupon = couponRepository.findByCode(request.getCouponCode())
                .orElseThrow(() -> new ApiException(ErrorCode.COUPON_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        if (coupon.isExpired(now)) {
            throw new ApiException(ErrorCode.COUPON_EXPIRED);
        }

        // 빠른 실패(UX) -> 최종 보장은 UNIQUE
        if (userCouponRepository.existsByUserIdAndCoupon_Id(userId, coupon.getId())) {
            throw new ApiException(ErrorCode.COUPON_ALREADY_USED);
        }

        // 선착순이면 재고 확보
        if (coupon.isFirstComeFirstServed()) {
            int updated = couponRepository.tryIncreaseIssuedCount(coupon.getId());
            if (updated == 0) {
                throw new ApiException(ErrorCode.COUPON_SOLD_OUT);
            }
        }

        // user_coupons insert
        try {
            UserCoupon userCoupon = UserCoupon.issue(userId, coupon, requestId);
            UserCoupon saved = userCouponRepository.save(userCoupon);

            log.info("쿠폰 발급 성공 : userId={}, couponId={}, userCouponId={}", userId, coupon.getId(), saved.getId());

            return ClaimCouponResponse.builder()
                    .userId(userId)
                    .couponId(coupon.getId())
                    .userCouponId(saved.getId())
                    .couponCode(coupon.getCode())
                    .title(coupon.getTitle())
                    .discountValue(coupon.getDiscountValue())
                    .build();
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 exists 체크를 통과해도 여기서 UNIQUE로 막힘
            throw new ApiException(ErrorCode.COUPON_ALREADY_USED);
        }
    }

    @Override
    public IssueCouponResponse use(Long userId, Long userCouponId) {
        UserCoupon userCoupon = userCouponRepository.findByIdAndUserId(userCouponId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.COUPON_NOT_FOUND));

        // 상태 체크
        if (userCoupon.getCouponStatus() == UserCouponStatus.USED) {
            throw new ApiException(ErrorCode.COUPON_ALREADY_USED);
        }
        if (userCoupon.getCouponStatus() != UserCouponStatus.ISSUED) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "사용할 수 없는 쿠폰 상태입니다.");
        }

        // 만료 체크
        Coupon coupon = userCoupon.getCoupon();
        if (coupon.isExpired(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.COUPON_EXPIRED);
        }

        // 사용 처리
        userCoupon.markUsed();

        return IssueCouponResponse.builder()
                .userId(userId)
                .userCouponId(userCouponId)
                .status(UserCouponStatus.USED.name())
                .build();
    }
}
