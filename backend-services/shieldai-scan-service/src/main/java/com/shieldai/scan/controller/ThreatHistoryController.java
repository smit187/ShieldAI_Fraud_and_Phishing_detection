package com.shieldai.scan.controller;

import com.shieldai.scan.dto.ThreatHistoryDto;
import com.shieldai.scan.service.ThreatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Threat history controller — provides paginated threat timeline for users.
 */
@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class ThreatHistoryController {

    private final ThreatHistoryService threatHistoryService;

    /**
     * GET /api/v1/history
     * Get the authenticated user's threat timeline.
     */
    @GetMapping
    public ResponseEntity<Page<ThreatHistoryDto>> getHistory(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = UUID.nameUUIDFromBytes(authentication.getName().getBytes());
        return ResponseEntity.ok(threatHistoryService.getHistory(userId, pageable));
    }

    /**
     * GET /api/v1/history?risk=HIGH
     * Filter threat history by risk label.
     */
    @GetMapping(params = "risk")
    public ResponseEntity<Page<ThreatHistoryDto>> getHistoryByRisk(
            Authentication authentication,
            @RequestParam String risk,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = UUID.nameUUIDFromBytes(authentication.getName().getBytes());
        return ResponseEntity.ok(threatHistoryService.getHistoryByRisk(userId, risk, pageable));
    }
}
