package com.zia.payments.coupon.repository;

import com.zia.payments.coupon.domain.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserIdAndCoupon_Id(Long userId, Long couponId);
    Optional<UserCoupon> findByIdAndUserId(Long userCouponId, Long userId);
}
