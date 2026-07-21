package com.shieldai.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private UUID userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String locale;
    private String avatarUrl;
    private Boolean isEmailVerified;
    private Map<String, Boolean> notificationPreferences;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginAt;
}
