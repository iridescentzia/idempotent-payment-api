package com.zia.payments.coupon.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_coupons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_coupon_once", columnNames = {"user_id", "coupon_id"})
        },
        indexes = {
                @Index(name = "idx_user_status", columnList = "user_id,coupon_status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_status", nullable = false, length = 20)
    private UserCouponStatus couponStatus;

    @Column(name = "issued_at", nullable = false, columnDefinition = "datetime(3)")
    private LocalDateTime issuedAt;

    @Column(name = "used_at", columnDefinition = "datetime(3)")
    private LocalDateTime usedAt;

    @Column(name = "request_id", length = 64)
    private String requestId;

    public static UserCoupon issue(Long userId, Coupon coupon, String requestId) {
        LocalDateTime now = LocalDateTime.now();
        return UserCoupon.builder()
                .userId(userId)
                .coupon(coupon)
                .couponStatus(UserCouponStatus.ISSUED)
                .issuedAt(now)
                .usedAt(null)
                .requestId(requestId)
                .build();
    }

    public void markUsed() {
        this.couponStatus = UserCouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
