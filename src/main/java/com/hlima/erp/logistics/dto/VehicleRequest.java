package com.hlima.erp.logistics.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record VehicleRequest(
        @NotBlank(message = "Placa é obrigatória") String plate,
        @NotBlank(message = "Modelo é obrigatório") String model,
        @DecimalMin(value = "0.00", message = "Capacidade não pode ser negativa") BigDecimal capacityKg
) {
}
