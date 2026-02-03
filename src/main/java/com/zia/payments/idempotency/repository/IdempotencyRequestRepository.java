package com.zia.payments.idempotency.repository;

import com.zia.payments.idempotency.domain.IdempotencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRequestRepository extends JpaRepository<IdempotencyRequest, Long> {

    // requestId 로 조회 (멱등성 핵심) -> unique(request_id)로 DB에서 중복 막음
    Optional<IdempotencyRequest> findByRequestId(String requestId);
}
