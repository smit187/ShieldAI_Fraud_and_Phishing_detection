package com.shieldai.scan.service;

import com.shieldai.scan.client.AiEngineClient;
import com.shieldai.scan.dto.ScanRequestDto;
import com.shieldai.scan.dto.ScanResultDto;
import com.shieldai.scan.entity.ScanLog;
import com.shieldai.scan.event.ScanCompletedEvent;
import com.shieldai.scan.kafka.ScanEventProducer;
import com.shieldai.scan.repository.ScanLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Core scan service — orchestrates the complete scan pipeline:
 * 1. Validate input
 * 2. Call AI Engine for risk scoring
 * 3. Persist scan result
 * 4. Publish Kafka event for async downstream processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScanService {

    private final AiEngineClient aiEngineClient;
    private final ScanLogRepository scanLogRepository;
    private final ScanEventProducer scanEventProducer;

    /**
     * Execute a full scan pipeline.
     */
    @Transactional
    public ScanResultDto scan(UUID userId, ScanRequestDto request) {
        log.info("Starting scan for user {} via {} channel", userId, request.getChannel());

        // 1. Call the AI Engine
        AiEngineClient.AiScanResult aiResult = aiEngineClient.scanContent(
                request.getContent(), request.getChannel());

        // 2. Map risk label
        ScanLog.RiskLabel riskLabel = mapRiskLabel(aiResult.getRisk_score());

        // 3. Persist scan log
        ScanLog scanLog = ScanLog.builder()
                .userId(userId)
                .channel(ScanLog.Channel.valueOf(request.getChannel().toUpperCase()))
                .contentHash(hashContent(request.getContent()))
                .contentSnippet(truncate(request.getContent(), 500))
                .senderInfo(request.getSenderInfo())
                .riskScore(aiResult.getRisk_score())
                .riskLabel(riskLabel)
                .aiModelVersion("v1.0.0-distilbert")
                .aiConfidence(BigDecimal.valueOf(0.95)) // Placeholder until AI engine returns confidence
                .aiExplanation(aiResult.getExplanation())
                .build();

        ScanLog savedLog = scanLogRepository.save(scanLog);
        log.info("Scan {} completed — risk: {} ({})", savedLog.getScanId(), riskLabel, aiResult.getRisk_score());

        // 4. Publish Kafka event (async)
        ScanCompletedEvent event = ScanCompletedEvent.builder()
                .scanId(savedLog.getScanId())
                .userId(userId)
                .channel(request.getChannel())
                .riskScore(aiResult.getRisk_score())
                .riskLabel(riskLabel.name())
                .aiConfidence(savedLog.getAiConfidence())
                .explanation(aiResult.getExplanation())
                .contentSnippet(savedLog.getContentSnippet())
                .scannedAt(savedLog.getScannedAt())
                .build();
        scanEventProducer.publishScanCompleted(event);

        // 5. Return result
        return ScanResultDto.builder()
                .scanId(savedLog.getScanId())
                .channel(request.getChannel())
                .riskScore(aiResult.getRisk_score())
                .riskLabel(riskLabel.name())
                .aiConfidence(savedLog.getAiConfidence())
                .explanation(aiResult.getExplanation())
                .aiModelVersion(savedLog.getAiModelVersion())
                .scannedAt(savedLog.getScannedAt())
                .build();
    }

    /**
     * Get scan history for a user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ScanResultDto> getScanHistory(UUID userId, Pageable pageable) {
        return scanLogRepository.findByUserIdOrderByScannedAtDesc(userId, pageable)
                .map(this::toScanResultDto);
    }

    /**
     * Submit user feedback on a scan (false positive reporting).
     */
    @Transactional
    public void submitFeedback(UUID scanId, String feedback) {
        scanLogRepository.findById(scanId).ifPresent(scanLog -> {
            scanLog.setUserFeedback(feedback);
            if ("FALSE_POSITIVE".equals(feedback)) {
                scanLog.setIsFalsePositive(true);
            }
            scanLogRepository.save(scanLog);
            log.info("Feedback '{}' recorded for scan {}", feedback, scanId);
        });
    }

    // --- Private helpers ---

    private ScanLog.RiskLabel mapRiskLabel(int riskScore) {
        if (riskScore <= 10) return ScanLog.RiskLabel.SAFE;
        if (riskScore <= 30) return ScanLog.RiskLabel.LOW;
        if (riskScore <= 60) return ScanLog.RiskLabel.MEDIUM;
        if (riskScore <= 85) return ScanLog.RiskLabel.HIGH;
        return ScanLog.RiskLabel.CRITICAL;
    }

    private String hashContent(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private ScanResultDto toScanResultDto(ScanLog log) {
        return ScanResultDto.builder()
                .scanId(log.getScanId())
                .channel(log.getChannel().name())
                .riskScore(log.getRiskScore())
                .riskLabel(log.getRiskLabel().name())
                .aiConfidence(log.getAiConfidence())
                .explanation(log.getAiExplanation())
                .aiModelVersion(log.getAiModelVersion())
                .scannedAt(log.getScannedAt())
                .build();
    }
}
