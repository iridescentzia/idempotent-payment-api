package com.zia.payments.coupon.service;

import com.zia.payments.coupon.dto.request.ClaimCouponRequest;
import com.zia.payments.coupon.dto.response.ClaimCouponResponse;
import com.zia.payments.coupon.dto.response.IssueCouponResponse;

public interface CouponService {

    // 선착순 발급
    ClaimCouponResponse claim(Long userId, ClaimCouponRequest request, String requestId);

    // 쿠폰 사용
    IssueCouponResponse use(Long userId, Long userCouponId);
}
