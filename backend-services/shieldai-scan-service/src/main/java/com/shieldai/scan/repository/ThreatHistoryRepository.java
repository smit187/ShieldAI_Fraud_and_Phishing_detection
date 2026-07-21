package com.shieldai.scan.repository;

import com.shieldai.scan.entity.ThreatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThreatHistoryRepository extends JpaRepository<ThreatHistory, UUID> {

    Page<ThreatHistory> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    Page<ThreatHistory> findByUserIdAndRiskLabelOrderByTimestampDesc(
            UUID userId, String riskLabel, Pageable pageable);
}
