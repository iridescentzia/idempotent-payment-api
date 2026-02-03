package com.zia.payments.point.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RedeemRequest {

    @NotNull(message = "amount는 필수입니다.")
    @Min(value = 1, message = "amount는 1 이상이어야 합니다.")
    private Long amount;

    private String memo;
    private String requestId;
}
