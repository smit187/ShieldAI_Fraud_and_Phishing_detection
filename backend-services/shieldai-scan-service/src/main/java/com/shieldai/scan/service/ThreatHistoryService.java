package com.shieldai.scan.service;

import com.shieldai.scan.dto.ThreatHistoryDto;
import com.shieldai.scan.repository.ThreatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Threat history service — provides paginated threat timeline for users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThreatHistoryService {

    private final ThreatHistoryRepository threatHistoryRepository;

    @Transactional(readOnly = true)
    public Page<ThreatHistoryDto> getHistory(UUID userId, Pageable pageable) {
        return threatHistoryRepository.findByUserIdOrderByTimestampDesc(userId, pageable)
                .map(history -> ThreatHistoryDto.builder()
                        .historyId(history.getHistoryId())
                        .sourceChannel(history.getSourceChannel())
                        .scanId(history.getScanId())
                        .riskScore(history.getRiskScore())
                        .riskLabel(history.getRiskLabel())
                        .summary(history.getSummary())
                        .timestamp(history.getTimestamp())
                        .build());
    }

    @Transactional(readOnly = true)
    public Page<ThreatHistoryDto> getHistoryByRisk(UUID userId, String riskLabel, Pageable pageable) {
        return threatHistoryRepository.findByUserIdAndRiskLabelOrderByTimestampDesc(
                userId, riskLabel, pageable)
                .map(history -> ThreatHistoryDto.builder()
                        .historyId(history.getHistoryId())
                        .sourceChannel(history.getSourceChannel())
                        .scanId(history.getScanId())
                        .riskScore(history.getRiskScore())
                        .riskLabel(history.getRiskLabel())
                        .summary(history.getSummary())
                        .timestamp(history.getTimestamp())
                        .build());
    }
}
