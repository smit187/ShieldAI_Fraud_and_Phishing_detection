package com.shieldai.user.dto;

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
public class DeviceResponse {

    private UUID deviceId;
    private String deviceToken;
    private String platform;
    private String osVersion;
    private String appVersion;
    private String modelName;
    private String modelVersion;
    private Boolean isActive;
    private LocalDateTime registeredAt;
    private LocalDateTime lastSyncedAt;
}
