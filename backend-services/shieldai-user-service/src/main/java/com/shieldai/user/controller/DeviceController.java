package com.shieldai.user.controller;

import com.shieldai.user.dto.DeviceRegistrationRequest;
import com.shieldai.user.dto.DeviceResponse;
import com.shieldai.user.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Device management controller — register, list, and remove devices for push notifications.
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * POST /api/v1/devices
     * Register a new device or update an existing one.
     */
    @PostMapping
    public ResponseEntity<DeviceResponse> registerDevice(
            Authentication authentication,
            @Valid @RequestBody DeviceRegistrationRequest request) {
        DeviceResponse response = deviceService.registerDevice(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/devices
     * List all active devices for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getDevices(Authentication authentication) {
        List<DeviceResponse> devices = deviceService.getDevices(authentication.getName());
        return ResponseEntity.ok(devices);
    }

    /**
     * DELETE /api/v1/devices/{deviceId}
     * Remove a specific device.
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> removeDevice(
            Authentication authentication,
            @PathVariable UUID deviceId) {
        deviceService.removeDevice(authentication.getName(), deviceId);
        return ResponseEntity.noContent().build();
    }
}
