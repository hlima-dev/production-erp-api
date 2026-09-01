package com.hlima.erp.logistics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleResponse(UUID id, String plate, String model, BigDecimal capacityKg, boolean active) {
}
