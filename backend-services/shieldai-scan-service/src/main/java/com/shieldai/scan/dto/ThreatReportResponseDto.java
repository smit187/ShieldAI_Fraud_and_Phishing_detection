package com.shieldai.scan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThreatReportResponseDto {

    private UUID reportId;
    private UUID userId;
    private String category;
    private String channel;
    private String contentReference;
    private String senderInfo;
    private String description;
    private String[] evidenceUrls;
    private String status;
    private Integer upvoteCount;
    private LocalDateTime submittedAt;
    private LocalDateTime resolvedAt;
}
