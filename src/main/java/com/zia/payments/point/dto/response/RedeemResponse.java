package com.zia.payments.point.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RedeemResponse {
    private Long userId;
    private Long redeemedAmount;
    private Long balanceAfter;
    private String memo;
}
