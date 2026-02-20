package com.zia.payments.coupon.repository;

import com.zia.payments.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);

    /**
     * 선착순 원자적 수량 확보
     * issued_count < total_quantity인 경우에만 +1
     * rowCount=1이면 성공, 0이면 품절
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Coupon c
        set c.issuedCount = c.issuedCount + 1 
        where c.id = :couponId
        and c.totalQuantity is not null
        and c.issuedCount < c.totalQuantity
    """)
    int tryIncreaseIssuedCount(Long couponId);
}
