package com.zia.payments.point.repository;

import com.zia.payments.point.domain.PointWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointWalletRepository extends JpaRepository<PointWallet, Long> {

    Optional<PointWallet> findByUserId(Long userId); // 유저 지갑 조회

    // SELECT FOR UPDATE
    @Query("SELECT pw FROM PointWallet pw WHERE pw.user.id = :userId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PointWallet> findByUserIdWithLock(@Param("userId") Long userId);
}
