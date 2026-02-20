package com.zia.payments.coupon.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IssueCouponResponse {
    private Long userId;
    private Long userCouponId;
    private String status; // USED
}
