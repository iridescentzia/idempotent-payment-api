package com.zia.payments.point.repository;

import com.zia.payments.point.domain.PointLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {

    @Query("SELECT pl FROM PointLedger pl WHERE pl.user.id = :userId ORDER BY pl.createdAt DESC")
    List<PointLedger> findTop20ByUserIdOrderByCreatedAtDesc(Long userId); // 최근 원장 20개 조회
}
