package com.hlima.erp.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public record WarehouseRequest(
        @NotBlank(message = "Código é obrigatório") String code,
        @NotBlank(message = "Nome é obrigatório") String name
) {
}
