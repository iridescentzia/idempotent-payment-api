package com.zia.payments.point.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ChargeResponse {
    private Long userId;
    private Long chargedAmount; // 충전한 금액
    private Long balanceAfter; // 충전 후 잔액
    private String memo;
}
