package com.shieldai.scan.kafka;

import com.shieldai.scan.entity.ThreatHistory;
import com.shieldai.scan.event.ScanCompletedEvent;
import com.shieldai.scan.repository.ThreatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer — listens for scan completion events and populates the threat history timeline.
 * Decouples the scan pipeline from the history aggregation for async processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScanEventConsumer {

    private final ThreatHistoryRepository threatHistoryRepository;

    @KafkaListener(
            topics = "scan-events",
            groupId = "scan-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleScanCompleted(ScanCompletedEvent event) {
        try {
            log.info("Received scan event for user {} — risk: {} ({})",
                    event.getUserId(), event.getRiskLabel(), event.getRiskScore());

            // Only record non-SAFE results in the threat history for relevance
            if (!"SAFE".equals(event.getRiskLabel())) {
                ThreatHistory history = ThreatHistory.builder()
                        .userId(event.getUserId())
                        .sourceChannel(event.getChannel())
                        .scanId(event.getScanId())
                        .riskScore(event.getRiskScore())
                        .riskLabel(event.getRiskLabel())
                        .summary(buildSummary(event))
                        .build();

                threatHistoryRepository.save(history);
                log.info("Threat history recorded for scan {}", event.getScanId());
            }
        } catch (Exception ex) {
            log.error("Failed to process scan event for scan {}: {}",
                    event.getScanId(), ex.getMessage(), ex);
            // In production, this would go to a dead-letter topic
        }
    }

    private String buildSummary(ScanCompletedEvent event) {
        return String.format("%s threat detected via %s channel. Risk score: %d/100. %s",
                event.getRiskLabel(),
                event.getChannel(),
                event.getRiskScore(),
                event.getExplanation() != null ? event.getExplanation() : "");
    }
}
