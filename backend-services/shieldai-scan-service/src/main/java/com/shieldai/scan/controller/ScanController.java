package com.shieldai.scan.controller;

import com.shieldai.scan.dto.ScanRequestDto;
import com.shieldai.scan.dto.ScanResultDto;
import com.shieldai.scan.service.ScanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Scan controller — handles content scanning and scan history endpoints.
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/scan")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;

    /**
     * POST /api/v1/scan
     * Submit content for AI-powered threat scanning.
     */
    @PostMapping
    public ResponseEntity<ScanResultDto> scanContent(
            Authentication authentication,
            @Valid @RequestBody ScanRequestDto request) {
        // In a real system, we'd resolve userId from the email via User Service or a shared cache.
        // For now, we generate a deterministic UUID from the email.
        UUID userId = UUID.nameUUIDFromBytes(authentication.getName().getBytes());
        ScanResultDto result = scanService.scan(userId, request);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/scan/history
     * Get paginated scan history for the authenticated user.
     */
    @GetMapping("/history")
    public ResponseEntity<Page<ScanResultDto>> getScanHistory(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = UUID.nameUUIDFromBytes(authentication.getName().getBytes());
        Page<ScanResultDto> history = scanService.getScanHistory(userId, pageable);
        return ResponseEntity.ok(history);
    }

    /**
     * POST /api/v1/scan/{scanId}/feedback
     * Submit user feedback on a scan result (e.g., false positive reporting).
     */
    @PostMapping("/{scanId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable UUID scanId,
            @RequestParam String feedback) {
        scanService.submitFeedback(scanId, feedback);
        return ResponseEntity.noContent().build();
    }
}
