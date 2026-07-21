package com.shieldai.scan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ScanLog entity — records every scan performed by the AI engine.
 * Stores risk assessment results and content metadata.
 */
@Entity
@Table(name = "scan_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "scan_id", updatable = false, nullable = false)
    private UUID scanId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private Channel channel;

    @Column(name = "content_hash", length = 255)
    private String contentHash;

    @Column(name = "content_snippet", length = 500)
    private String contentSnippet;

    @Column(name = "sender_info", length = 255)
    private String senderInfo;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_label", nullable = false, length = 20)
    private RiskLabel riskLabel;

    @Column(name = "ai_model_version", length = 50)
    private String aiModelVersion;

    @Column(name = "ai_confidence", precision = 5, scale = 4)
    private BigDecimal aiConfidence;

    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    @Column(name = "is_false_positive")
    @Builder.Default
    private Boolean isFalsePositive = false;

    @Column(name = "user_feedback", length = 20)
    private String userFeedback;

    @CreatedDate
    @Column(name = "scanned_at", updatable = false)
    private LocalDateTime scannedAt;

    public enum Channel {
        SMS, EMAIL, URL, QR, CALL, NOTIFICATION, AD, MANUAL
    }

    public enum RiskLabel {
        SAFE, LOW, MEDIUM, HIGH, CRITICAL
    }
}
