package com.shieldai.scan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ThreatHistory entity — aggregated threat timeline per user.
 * Populated asynchronously via Kafka consumer after each scan.
 */
@Entity
@Table(name = "threat_history")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ThreatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id", updatable = false, nullable = false)
    private UUID historyId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "source_channel", nullable = false, length = 30)
    private String sourceChannel;

    @Column(name = "scan_id")
    private UUID scanId;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "risk_label", nullable = false, length = 20)
    private String riskLabel;

    @Column(name = "summary", length = 500)
    private String summary;

    @CreatedDate
    @Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp;
}
