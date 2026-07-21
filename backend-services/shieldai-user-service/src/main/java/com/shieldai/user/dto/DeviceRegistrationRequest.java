package com.shieldai.user.dto;

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
public class DeviceRegistrationRequest {

    @NotBlank(message = "Device token is required")
    private String deviceToken;

    @NotNull(message = "Platform is required")
    private String platform; // ANDROID, IOS, WEB

    private String osVersion;
    private String appVersion;
    private String modelName;
    private String modelVersion;
}
