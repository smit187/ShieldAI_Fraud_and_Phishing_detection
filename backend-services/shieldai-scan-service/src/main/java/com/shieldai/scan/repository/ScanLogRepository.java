package com.shieldai.scan.repository;

import com.shieldai.scan.entity.ScanLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScanLogRepository extends JpaRepository<ScanLog, UUID> {

    Page<ScanLog> findByUserIdOrderByScannedAtDesc(UUID userId, Pageable pageable);

    Page<ScanLog> findByUserIdAndChannelOrderByScannedAtDesc(
            UUID userId, ScanLog.Channel channel, Pageable pageable);

    List<ScanLog> findByContentHash(String contentHash);

    @Query("SELECT COUNT(s) FROM ScanLog s WHERE s.userId = :userId AND s.scannedAt >= :since")
    long countScansSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    @Query("SELECT s.riskLabel, COUNT(s) FROM ScanLog s WHERE s.userId = :userId GROUP BY s.riskLabel")
    List<Object[]> getRiskDistribution(@Param("userId") UUID userId);
}
