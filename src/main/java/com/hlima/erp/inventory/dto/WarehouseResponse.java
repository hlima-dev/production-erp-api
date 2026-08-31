package com.hlima.erp.inventory.dto;

import java.util.UUID;

public record WarehouseResponse(UUID id, String code, String name, boolean active) {
}
