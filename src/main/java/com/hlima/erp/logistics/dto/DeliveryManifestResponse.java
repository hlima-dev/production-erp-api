package com.hlima.erp.logistics.dto;

import com.hlima.erp.logistics.entity.DeliveryManifestStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DeliveryManifestResponse(
        UUID id,
        Long manifestNumber,
        UUID vehicleId,
        String vehiclePlate,
        UUID driverId,
        String driverName,
        DeliveryManifestStatus status,
        List<ManifestOrderResponse> orders,
        Instant departedAt,
        Instant completedAt,
        String notes,
        Instant createdAt
) {
}
