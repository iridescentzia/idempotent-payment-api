package com.zia.payments.point.repository;

import com.zia.payments.point.domain.PointLedger;
import com.zia.payments.point.domain.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointLedgerRepository extends JpaRepository<PointWallet, Long> {
    List<PointLedger> findTop20ByUserIdOrderByCreatedAtDesc(Long userId); // 최근 원장 20개 조회
}
