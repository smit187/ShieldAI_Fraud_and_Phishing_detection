package com.shieldai.user.controller;

import com.shieldai.user.dto.UpdateProfileRequest;
import com.shieldai.user.dto.UserProfileResponse;
import com.shieldai.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * User profile controller — authenticated endpoints for managing user profiles.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/v1/users/me
     * Get the authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        UserProfileResponse profile = userService.getProfile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/v1/users/me
     * Update the authenticated user's profile (partial update — only non-null fields).
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse updated = userService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/v1/users/me
     * Deactivate the authenticated user's account (soft delete).
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        userService.deleteAccount(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
