package com.hlima.erp.sales.dto;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String document,
        String name,
        String email,
        String phone,
        String address,
        boolean active
) {
}
