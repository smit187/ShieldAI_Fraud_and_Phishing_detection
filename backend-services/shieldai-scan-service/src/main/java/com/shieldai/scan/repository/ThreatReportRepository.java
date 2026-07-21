package com.shieldai.scan.repository;

import com.shieldai.scan.entity.ThreatReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThreatReportRepository extends JpaRepository<ThreatReport, UUID> {

    Page<ThreatReport> findByUserIdOrderBySubmittedAtDesc(UUID userId, Pageable pageable);

    Page<ThreatReport> findByStatusOrderBySubmittedAtDesc(ThreatReport.Status status, Pageable pageable);

    Page<ThreatReport> findByCategoryOrderBySubmittedAtDesc(ThreatReport.Category category, Pageable pageable);

    @Modifying
    @Query("UPDATE ThreatReport tr SET tr.upvoteCount = tr.upvoteCount + 1 WHERE tr.reportId = :reportId")
    void incrementUpvoteCount(@Param("reportId") UUID reportId);
}
