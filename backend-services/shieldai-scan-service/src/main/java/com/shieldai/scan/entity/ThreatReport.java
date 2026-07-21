package com.shieldai.scan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ThreatReport entity — community-submitted reports of phishing/scam content.
 * Users can report suspicious content they encounter, which is reviewed by moderators.
 */
@Entity
@Table(name = "threat_reports")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ThreatReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id", updatable = false, nullable = false)
    private UUID reportId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private Category category;

    @Column(name = "channel", length = 30)
    private String channel;

    @Column(name = "content_reference", columnDefinition = "TEXT")
    private String contentReference;

    @Column(name = "sender_info", length = 255)
    private String senderInfo;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "evidence_urls", columnDefinition = "TEXT[]")
    private String[] evidenceUrls;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "upvote_count")
    @Builder.Default
    private Integer upvoteCount = 0;

    @CreatedDate
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    public enum Category {
        PHISHING, SCAM, SPAM, MALWARE, IMPERSONATION, OTHER
    }

    public enum Status {
        PENDING, REVIEWING, CONFIRMED, REJECTED, RESOLVED
    }
}
