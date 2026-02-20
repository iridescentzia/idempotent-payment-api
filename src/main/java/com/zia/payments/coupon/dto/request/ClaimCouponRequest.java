package com.zia.payments.coupon.dto.request;

import lombok.Getter;

@Getter
public class ClaimCouponRequest {

    // 선착순 쿠폰은 code로 요청
    private String couponCode;
}
