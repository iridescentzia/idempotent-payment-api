package com.zia.payments.idempotency.service;

import com.zia.payments.global.exception.ApiException;
import com.zia.payments.global.exception.ErrorCode;
import com.zia.payments.idempotency.domain.IdempotencyRequest;
import com.zia.payments.idempotency.domain.IdempotencyStatus;
import com.zia.payments.idempotency.util.IdempotencyTxExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyTxExecutor tx;

    @Override
    public IdempotencyRequest findByRequestId(String requestId) {
        return tx.findOrNull(requestId);
    }

    @Override
    public IdempotencyRequest createInProgress(Long userId, String requestId, String endpoint) {
        try {
            // INSERT는 REQUIRES_NEW에서 실행
            return tx.insertInProgress(userId, requestId, endpoint);
        } catch (DataIntegrityViolationException e) {
            // 조회도 REQUIRES_NEW에서 실행 -> 500 error 방지
            IdempotencyRequest existing = tx.load(requestId);

            log.info("멱등키 이미 존재 : requestId={}, status={}", requestId, existing.getIdempotencyStatus());

            // 상태별 처리
            if(existing.getIdempotencyStatus() == IdempotencyStatus.IN_PROGRESS) {
                throw new ApiException(ErrorCode.IDEMPOTENCY_IN_PROGRESS);
            }

            if(existing.getIdempotencyStatus() == IdempotencyStatus.FAILED) {
                throw new ApiException(ErrorCode.IDEMPOTENCY_FAILED);
            }

            // SUCCESS인 경우만 반환
            return existing;
        }
    }

    @Override
    public void markSuccess(String requestId, String responseBody) {
        tx.markSuccess(requestId, responseBody);
    }

    @Override
    public void markFailed(String requestId) {
        tx.markFailed(requestId);
    }
}
