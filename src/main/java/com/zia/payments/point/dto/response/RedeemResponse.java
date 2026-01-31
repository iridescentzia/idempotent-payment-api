package com.zia.payments.point.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RedeemResponse {
    private Long userId;
    private Long redeemedAmount;
    private Long balanceAfter;
    private String memo;
}
