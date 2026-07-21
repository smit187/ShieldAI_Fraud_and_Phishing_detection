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
public class ThreatHistoryDto {

    private UUID historyId;
    private String sourceChannel;
    private UUID scanId;
    private Integer riskScore;
    private String riskLabel;
    private String summary;
    private LocalDateTime timestamp;
}
