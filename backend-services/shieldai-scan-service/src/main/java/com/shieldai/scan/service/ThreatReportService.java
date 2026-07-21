package com.shieldai.scan.service;

import com.shieldai.scan.dto.ThreatReportRequestDto;
import com.shieldai.scan.dto.ThreatReportResponseDto;
import com.shieldai.scan.entity.ThreatReport;
import com.shieldai.scan.repository.ThreatReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Threat report service — handles community-submitted threat reports.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThreatReportService {

    private final ThreatReportRepository threatReportRepository;

    @Transactional
    public ThreatReportResponseDto submitReport(UUID userId, ThreatReportRequestDto request) {
        ThreatReport report = ThreatReport.builder()
                .userId(userId)
                .category(ThreatReport.Category.valueOf(request.getCategory().toUpperCase()))
                .channel(request.getChannel())
                .contentReference(request.getContentReference())
                .senderInfo(request.getSenderInfo())
                .description(request.getDescription())
                .evidenceUrls(request.getEvidenceUrls())
                .build();

        ThreatReport saved = threatReportRepository.save(report);
        log.info("Threat report {} submitted by user {}", saved.getReportId(), userId);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ThreatReportResponseDto> getUserReports(UUID userId, Pageable pageable) {
        return threatReportRepository.findByUserIdOrderBySubmittedAtDesc(userId, pageable)
                .map(this::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ThreatReportResponseDto> getReportsByStatus(String status, Pageable pageable) {
        return threatReportRepository.findByStatusOrderBySubmittedAtDesc(
                ThreatReport.Status.valueOf(status.toUpperCase()), pageable)
                .map(this::toResponseDto);
    }

    @Transactional
    public void upvoteReport(UUID reportId) {
        threatReportRepository.incrementUpvoteCount(reportId);
    }

    private ThreatReportResponseDto toResponseDto(ThreatReport report) {
        return ThreatReportResponseDto.builder()
                .reportId(report.getReportId())
                .userId(report.getUserId())
                .category(report.getCategory().name())
                .channel(report.getChannel())
                .contentReference(report.getContentReference())
                .senderInfo(report.getSenderInfo())
                .description(report.getDescription())
                .evidenceUrls(report.getEvidenceUrls())
                .status(report.getStatus().name())
                .upvoteCount(report.getUpvoteCount())
                .submittedAt(report.getSubmittedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}
