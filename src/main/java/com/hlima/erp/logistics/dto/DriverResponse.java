package com.hlima.erp.logistics.dto;

import java.util.UUID;

public record DriverResponse(UUID id, String name, String document, String license, String phone, boolean active) {
}
