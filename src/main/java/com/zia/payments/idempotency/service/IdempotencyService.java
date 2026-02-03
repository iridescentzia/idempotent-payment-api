package com.zia.payments.idempotency.service;

import com.zia.payments.idempotency.domain.IdempotencyRequest;

public interface IdempotencyService {
    // 멱등키 조회
    IdempotencyRequest findByRequestId(String requestId);

    /**
     * 선점
     * 멱등키 없으면 : IN_PROGRESS insert
     * 멱등키 있으면 : 기존 row 반환
     */
    IdempotencyRequest createInProgress(Long userId, String requestId, String endpoint);

    // SUCCESS
    void markSuccess(String requestId, String responseBody);

    // FAILED
    void markFailed(String requestId);
}
