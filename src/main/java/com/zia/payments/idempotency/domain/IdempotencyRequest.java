package com.zia.payments.idempotency.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "idempotency_requests",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_idempotency_request_id", columnNames = {"request_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class IdempotencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idempotency_id")
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true, length = 64)
    private String requestId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "endpoint", nullable = false, length = 100)
    private String endpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "idempotency_status", nullable = false, length = 20)
    private IdempotencyStatus idempotencyStatus;

    @Column(name = "response_body", columnDefinition = "json")
    private String responseBody;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // status 실수 방지
        if(this.idempotencyStatus == null) {
            this.idempotencyStatus = IdempotencyStatus.IN_PROGRESS;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static IdempotencyRequest inProgress(String requestId, Long userId, String endpoint) {
        return IdempotencyRequest.builder()
                .requestId(requestId)
                .userId(userId)
                .endpoint(endpoint)
                .idempotencyStatus(IdempotencyStatus.IN_PROGRESS)
                .responseBody(null)
                .build();
    }

    public void markSuccess(String responseBody) {
        this.idempotencyStatus = IdempotencyStatus.SUCCESS;
        this.responseBody = responseBody;
    }

    public void markFailed() {
        this.idempotencyStatus = IdempotencyStatus.FAILED;
    }
}
