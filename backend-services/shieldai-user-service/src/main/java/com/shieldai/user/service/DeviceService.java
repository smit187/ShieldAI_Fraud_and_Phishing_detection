package com.shieldai.user.service;

import com.shieldai.user.dto.DeviceRegistrationRequest;
import com.shieldai.user.dto.DeviceResponse;
import com.shieldai.user.entity.Device;
import com.shieldai.user.entity.User;
import com.shieldai.user.exception.ResourceNotFoundException;
import com.shieldai.user.repository.DeviceRepository;
import com.shieldai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Device management service — register, list, and remove devices for push notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    private static final int MAX_DEVICES_PER_USER = 5;

    /**
     * Register a new device or update an existing one (by device token).
     */
    @Transactional
    public DeviceResponse registerDevice(String email, DeviceRegistrationRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Check if device already exists (by token) — update if so
        return deviceRepository.findByDeviceToken(request.getDeviceToken())
                .map(existing -> {
                    existing.setOsVersion(request.getOsVersion());
                    existing.setAppVersion(request.getAppVersion());
                    existing.setModelVersion(request.getModelVersion());
                    existing.setLastSyncedAt(LocalDateTime.now());
                    existing.setIsActive(true);
                    Device updated = deviceRepository.save(existing);
                    log.info("Device updated for user: {}", email);
                    return toDeviceResponse(updated);
                })
                .orElseGet(() -> {
                    // Check device limit
                    if (deviceRepository.countByUser_UserId(user.getUserId()) >= MAX_DEVICES_PER_USER) {
                        throw new IllegalStateException(
                                "Maximum number of devices (" + MAX_DEVICES_PER_USER + ") reached");
                    }

                    Device device = Device.builder()
                            .user(user)
                            .deviceToken(request.getDeviceToken())
                            .platform(Device.Platform.valueOf(request.getPlatform().toUpperCase()))
                            .osVersion(request.getOsVersion())
                            .appVersion(request.getAppVersion())
                            .modelName(request.getModelName())
                            .modelVersion(request.getModelVersion())
                            .lastSyncedAt(LocalDateTime.now())
                            .build();

                    Device saved = deviceRepository.save(device);
                    log.info("New device registered for user: {} ({})", email, request.getPlatform());
                    return toDeviceResponse(saved);
                });
    }

    /**
     * Get all active devices for the current user.
     */
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevices(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return deviceRepository.findByUser_UserIdAndIsActiveTrue(user.getUserId())
                .stream()
                .map(this::toDeviceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Remove a specific device by ID.
     */
    @Transactional
    public void removeDevice(String email, UUID deviceId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        deviceRepository.deleteByUser_UserIdAndDeviceId(user.getUserId(), deviceId);
        log.info("Device {} removed for user: {}", deviceId, email);
    }

    // --- Private helpers ---

    private DeviceResponse toDeviceResponse(Device device) {
        return DeviceResponse.builder()
                .deviceId(device.getDeviceId())
                .deviceToken(device.getDeviceToken())
                .platform(device.getPlatform().name())
                .osVersion(device.getOsVersion())
                .appVersion(device.getAppVersion())
                .modelName(device.getModelName())
                .modelVersion(device.getModelVersion())
                .isActive(device.getIsActive())
                .registeredAt(device.getRegisteredAt())
                .lastSyncedAt(device.getLastSyncedAt())
                .build();
    }
}
