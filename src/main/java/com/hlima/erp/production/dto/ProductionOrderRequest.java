package com.hlima.erp.production.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductionOrderRequest(
        @NotNull(message = "Produto é obrigatório") UUID productId,
        @NotNull(message = "Almoxarifado é obrigatório") UUID warehouseId,
        @NotNull(message = "Quantidade planejada é obrigatória")
        @DecimalMin(value = "0.000001", message = "Quantidade planejada deve ser maior que zero") BigDecimal plannedQuantity,
        String notes
) {
}
