package com.shieldai.user.service;

import com.shieldai.user.dto.*;
import com.shieldai.user.entity.RefreshToken;
import com.shieldai.user.entity.User;
import com.shieldai.user.exception.InvalidCredentialsException;
import com.shieldai.user.exception.InvalidTokenException;
import com.shieldai.user.exception.UserAlreadyExistsException;
import com.shieldai.user.repository.RefreshTokenRepository;
import com.shieldai.user.repository.UserRepository;
import com.shieldai.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Authentication service — handles registration, login, token refresh, and logout.
 * Implements secure JWT token rotation with refresh token persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    /**
     * Register a new user — validates uniqueness, hashes password, generates tokens.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("An account with this email already exists");
        }

        // Check if phone number already exists (if provided)
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException("An account with this phone number already exists");
        }

        // Create user entity
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .locale(request.getLocale() != null ? request.getLocale() : "en")
                .notificationPreferences(Map.of("push", true, "email", true, "sms", false))
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getEmail());
        String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();

        // Persist refresh token
        saveRefreshToken(savedUser, refreshTokenValue, null);

        return AuthResponse.of(accessToken, refreshTokenValue, accessTokenExpirationMs, toProfileResponse(savedUser));
    }

    /**
     * Authenticate user with email/password — validates credentials, generates new tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

            // Update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();

            // Persist refresh token
            saveRefreshToken(user, refreshTokenValue, request.getDeviceInfo());

            log.info("User logged in: {}", user.getEmail());
            return AuthResponse.of(accessToken, refreshTokenValue, accessTokenExpirationMs, toProfileResponse(user));

        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    /**
     * Refresh access token — validates refresh token, rotates it (single-use), issues new pair.
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!storedToken.isValid()) {
            // If someone tries to reuse a revoked token, revoke ALL tokens for the user (security measure)
            if (storedToken.getIsRevoked()) {
                refreshTokenRepository.revokeAllByUserId(storedToken.getUser().getUserId());
                log.warn("Refresh token reuse detected for user: {}. All tokens revoked.",
                        storedToken.getUser().getEmail());
            }
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        // Revoke old refresh token (single-use rotation)
        storedToken.setIsRevoked(true);
        refreshTokenRepository.save(storedToken);

        // Generate new token pair
        User user = storedToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String newRefreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();

        saveRefreshToken(user, newRefreshTokenValue, storedToken.getDeviceInfo());

        log.info("Token refreshed for user: {}", user.getEmail());
        return AuthResponse.of(newAccessToken, newRefreshTokenValue, accessTokenExpirationMs, toProfileResponse(user));
    }

    /**
     * Logout — revokes all refresh tokens for the user.
     */
    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        refreshTokenRepository.revokeAllByUserId(user.getUserId());
        log.info("User logged out (all tokens revoked): {}", email);
    }

    // --- Private helpers ---

    private void saveRefreshToken(User user, String tokenValue, String deviceInfo) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtTokenProvider.getRefreshTokenExpirationMs() / 1000))
                .deviceInfo(deviceInfo)
                .build();
        refreshTokenRepository.save(refreshToken);
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
