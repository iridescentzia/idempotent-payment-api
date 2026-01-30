package com.zia.payments.point.domain;

import com.zia.payments.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "point_ledger",
        indexes = {
                @Index(name = "idx_ledger_user_created", columnList = "user_id, created_at"), // 최근 원장 조회
                @Index(name = "idx_ledger_request", columnList = "request_id") // 멱등성 추적
        }
)
public class PointLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ledger_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PointLedgerType type; // CHARGE, REDEEM, REFUND

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "ref_type", length = 20)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "memo", length = 255)
    private String memo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /** 팩토리 메서드 : 충전용
     * type을 자동으로 CHARGE로 설정
     * Service에서 type을 신경 쓸 필요 없음
     * CHARGE 생성 규칙이 변경되면 여기만 수정
     * 도메인 로직은 Domain에 캡슐화
     */
    public static PointLedger charge(User user, Long amount, Long balanceAfter, String memo) {
        return PointLedger.builder()
                .user(user)
                .type(PointLedgerType.CHARGE) // 자동설정 (비즈니스 로직)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .memo(memo)
                .build();
    }

    // 팩토리 메서드 : 차감용
    public static PointLedger redeem(User user, Long amount, Long balanceAfter, String memo) {
        return PointLedger.builder()
                .user(user)
                .type(PointLedgerType.REDEEM)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .memo(memo)
                .build();
    }
}
