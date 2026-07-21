package com.shieldai.scan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScanResultDto {

    private UUID scanId;
    private String channel;
    private Integer riskScore;
    private String riskLabel;
    private BigDecimal aiConfidence;
    private String explanation;
    private String aiModelVersion;
    private LocalDateTime scannedAt;
}
