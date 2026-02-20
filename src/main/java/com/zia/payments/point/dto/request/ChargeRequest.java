package com.zia.payments.point.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChargeRequest {
    private Long amount; // 충전 금액
    private String memo;
}
