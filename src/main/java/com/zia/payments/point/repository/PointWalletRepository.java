package com.zia.payments.point.repository;

import com.zia.payments.point.domain.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointWalletRepository extends JpaRepository<PointWallet, Long> {
    Optional<PointWallet> findByUserId(Long userId);
}
