package com.zia.payments.point.service;

import com.zia.payments.point.dto.response.ChargeResponse;
import com.zia.payments.point.dto.response.RedeemResponse;

public interface PointService {

    ChargeResponse charge(Long userId, Long amount, String memo); // DTO 반환
    Long getBalance(Long userId);
    RedeemResponse redeem(Long userId, Long amount, String memo, String requestId);
    RedeemResponse redeemNoLock(Long userId, Long amount, String memo);
}
