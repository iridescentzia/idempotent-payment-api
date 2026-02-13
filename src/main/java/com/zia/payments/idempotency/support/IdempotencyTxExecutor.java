package com.zia.payments.idempotency.support;

import com.zia.payments.global.exception.ApiException;
import com.zia.payments.global.exception.ErrorCode;
import com.zia.payments.idempotency.domain.IdempotencyRequest;
import com.zia.payments.idempotency.repository.IdempotencyRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyTxExecutor {

    private final IdempotencyRequestRepository repo;

    // UNIQUE 충돌을 즉시 발생시키기 위해 flush 포함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRequest insertInProgress(Long userId, String requestId, String endpoint) {
        IdempotencyRequest req = IdempotencyRequest.inProgress(requestId, userId, endpoint);
        return repo.saveAndFlush(req);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public IdempotencyRequest load(String requestId) {
        return repo.findByRequestId(requestId)
                .orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR, "UNIQUE 충돌 후 조회 실패"));
    }

    @Transactional(readOnly = true)
    public IdempotencyRequest findOrNull(String requestId) {
        return repo.findByRequestId(requestId).orElse(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(String requestId, String responseBody) {
        IdempotencyRequest req = repo.findByRequestId(requestId)
                        .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "멱등키 조회 실패"));
        req.markSuccess(responseBody);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String requestId) {
        IdempotencyRequest req = repo.findByRequestId(requestId)
                        .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "멱등키 조회 실패"));
        req.markFailed();
    }
}
