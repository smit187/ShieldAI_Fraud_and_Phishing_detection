package com.shieldai.user.service;

import com.shieldai.user.dto.UpdateProfileRequest;
import com.shieldai.user.dto.UserProfileResponse;
import com.shieldai.user.entity.User;
import com.shieldai.user.exception.ResourceNotFoundException;
import com.shieldai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User profile management service — get, update, and delete user profiles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get the current user's profile by email (extracted from JWT).
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = findUserByEmail(email);
        return toProfileResponse(user);
    }

    /**
     * Update the current user's profile — only non-null fields are updated (partial update).
     */
    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUserByEmail(email);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getLocale() != null) {
            user.setLocale(request.getLocale());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getNotificationPreferences() != null) {
            user.setNotificationPreferences(request.getNotificationPreferences());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", email);
        return toProfileResponse(updatedUser);
    }

    /**
     * Soft-delete the current user's account — deactivates instead of hard delete.
     */
    @Transactional
    public void deleteAccount(String email) {
        User user = findUserByEmail(email);
        userRepository.deactivateUser(user.getUserId());
        log.info("Account deactivated for user: {}", email);
    }

    // --- Private helpers ---

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .locale(user.getLocale())
                .avatarUrl(user.getAvatarUrl())
                .isEmailVerified(user.getIsEmailVerified())
                .notificationPreferences(user.getNotificationPreferences())
                .registrationDate(user.getRegistrationDate())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
