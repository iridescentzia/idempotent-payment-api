package com.zia.payments.coupon.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClaimCouponResponse {
    private Long userId;
    private Long couponId;
    private Long userCouponId;
    private String couponCode;
    private String title;
    private Long discountValue;
}
