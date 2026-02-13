package com.zia.payments.idempotency.service;

import com.zia.payments.global.exception.ApiException;
import com.zia.payments.global.exception.ErrorCode;
import com.zia.payments.idempotency.domain.IdempotencyRequest;
import com.zia.payments.idempotency.domain.IdempotencyStatus;
import com.zia.payments.idempotency.repository.IdempotencyRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyRequestRepository idempotencyRequestRepository;

    @Override
    @Transactional(readOnly = true)
    public IdempotencyRequest findByRequestId(String requestId) {
        return idempotencyRequestRepository.findByRequestId(requestId).orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRequest createInProgress(Long userId, String requestId, String endpoint) {
        try {
            // 선점 시 insert 먼저
            IdempotencyRequest req = IdempotencyRequest.inProgress(requestId, userId, endpoint);
            IdempotencyRequest saved = idempotencyRequestRepository.save(req);

            log.info("멱등키 선점 성공(IN_PROGRESS): requestId={}, userId={}, endpoint={}", requestId, userId, endpoint);

            return saved;
        } catch (DataIntegrityViolationException e) {
            // 이미 선점 당항 -> 기존 row 반환
            IdempotencyRequest existing = idempotencyRequestRepository.findByRequestId(requestId)
                    .orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR, "멱등키 UNIQUE 충돌 후 조회 실패"));

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

    // REQUIRES_NEW 적용 (독립 트랜잭션)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(String requestId, String responseBody) {
        IdempotencyRequest req = idempotencyRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "멱등키 조회 실패"));

        req.markSuccess(responseBody);
        // save 필요 x

        log.info("멱등키 SUCCESS : requestId={}", requestId);
    }

    // REQUIRES_NEW 적용 (독립 트랜잭션)
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String requestId) {
        IdempotencyRequest req = idempotencyRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "멱등키 조회 실패"));

        req.markFailed();
        // save 필요 x

        log.info("멱등키 FAILED : requestId={}", requestId);
    }
}
