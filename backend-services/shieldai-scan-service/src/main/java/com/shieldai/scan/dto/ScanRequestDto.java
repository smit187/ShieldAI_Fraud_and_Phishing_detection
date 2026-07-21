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
public class ScanRequestDto {

    @NotBlank(message = "Content to scan is required")
    private String content;

    @NotNull(message = "Channel is required")
    private String channel; // SMS, EMAIL, URL, QR, CALL, NOTIFICATION, AD, MANUAL

    private String senderInfo;
}
