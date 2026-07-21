package com.shieldai.scan.controller;

import com.shieldai.scan.dto.ThreatReportRequestDto;
import com.shieldai.scan.dto.ThreatReportResponseDto;
import com.shieldai.scan.service.ThreatReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Threat report controller — community crowdsourcing endpoints.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ThreatReportController {

    private final ThreatReportService threatReportService;

    /**
     * POST /api/v1/reports
     * Submit a new threat report.
     */
    @PostMapping
    public ResponseEntity<ThreatReportResponseDto> submitReport(
            Authentication authentication,
            @Valid @RequestBody ThreatReportRequestDto request) {
        UUID userId = UUID.nameUUIDFromBytes(authentication.getName().getBytes());
        ThreatReportResponseDto report = threatReportService.submitReport(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    /**
     * GET /api/v1/reports/my
     * Get the authenticated user's submitted reports.
     */
    @GetMapping("/my")
    public ResponseEntity<Page<ThreatReportResponseDto>> getMyReports(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = UUID.nameUUIDFromBytes(authentication.getName().getBytes());
        return ResponseEntity.ok(threatReportService.getUserReports(userId, pageable));
    }

    /**
     * GET /api/v1/reports?status=PENDING
     * Get reports filtered by status (for moderators/admins).
     */
    @GetMapping
    public ResponseEntity<Page<ThreatReportResponseDto>> getReportsByStatus(
            @RequestParam(defaultValue = "PENDING") String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(threatReportService.getReportsByStatus(status, pageable));
    }

    /**
     * POST /api/v1/reports/{reportId}/upvote
     * Upvote a community threat report.
     */
    @PostMapping("/{reportId}/upvote")
    public ResponseEntity<Void> upvoteReport(@PathVariable UUID reportId) {
        threatReportService.upvoteReport(reportId);
        return ResponseEntity.noContent().build();
    }
}
