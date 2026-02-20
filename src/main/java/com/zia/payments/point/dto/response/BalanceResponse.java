package com.zia.payments.point.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BalanceResponse {
    private Long userId;
    private Long balance;
}
