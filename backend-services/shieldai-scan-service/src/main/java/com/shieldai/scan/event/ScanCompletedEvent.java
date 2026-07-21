package com.shieldai.scan.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka event published after each scan is completed.
 * Consumed by the ThreatHistory aggregation consumer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanCompletedEvent implements Serializable {

    private UUID scanId;
    private UUID userId;
    private String channel;
    private Integer riskScore;
    private String riskLabel;
    private BigDecimal aiConfidence;
    private String explanation;
    private String contentSnippet;
    private LocalDateTime scannedAt;
}
