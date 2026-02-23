package com.zia.payments.coupon.controller;

import com.zia.payments.coupon.dto.request.ClaimCouponRequest;
import com.zia.payments.coupon.dto.response.ClaimCouponResponse;
import com.zia.payments.coupon.dto.response.IssueCouponResponse;
import com.zia.payments.coupon.service.CouponService;
import com.zia.payments.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/coupons")
public class CouponController {

    private final CouponService couponService;

    /**
     * 선착순 발급 : POST /api/users/{userId}/coupons/claim
     * Header : Idempotency-Key
     */
    @PostMapping("/claim")
    public ApiResponse<ClaimCouponResponse> claim(
            @PathVariable Long userId,
            @RequestBody ClaimCouponRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String requestId
    ) {
        return ApiResponse.success(couponService.claim(userId, request, requestId));
    }

    /**
     * 쿠폰 사용 : POST /api/users/{userId}/coupons/{userCouponId}/use
     */
    @PostMapping("/{userCouponId}/use")
    public ApiResponse<IssueCouponResponse> use(
            @PathVariable Long userId,
            @PathVariable Long userCouponId
    ) {
        return ApiResponse.success(couponService.use(userId, userCouponId));
    }
}
