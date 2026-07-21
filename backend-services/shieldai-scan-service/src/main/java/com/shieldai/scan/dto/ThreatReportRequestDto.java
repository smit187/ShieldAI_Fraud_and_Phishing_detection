package com.shieldai.scan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatReportRequestDto {

    @NotNull(message = "Category is required")
    private String category; // PHISHING, SCAM, SPAM, MALWARE, IMPERSONATION, OTHER

    private String channel;

    @NotBlank(message = "Content reference is required")
    private String contentReference;

    private String senderInfo;
    private String description;
    private String[] evidenceUrls;
}
