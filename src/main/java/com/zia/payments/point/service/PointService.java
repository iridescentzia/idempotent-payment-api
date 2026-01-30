package com.zia.payments.point.service;

import com.zia.payments.point.dto.response.ChargeResponse;

public interface PointService {

    ChargeResponse charge(Long userId, Long amount, String memo); // DTO 반환
    Long getBalance(Long userId);
}
