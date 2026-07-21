package com.shieldai.user.repository;

import com.shieldai.user.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    List<Device> findByUser_UserIdAndIsActiveTrue(UUID userId);

    Optional<Device> findByDeviceToken(String deviceToken);

    long countByUser_UserId(UUID userId);

    void deleteByUser_UserIdAndDeviceId(UUID userId, UUID deviceId);
}
