package com.hlima.erp.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record StockMovementRequest(
        @NotNull(message = "Produto é obrigatório") UUID productId,
        @NotNull(message = "Almoxarifado é obrigatório") UUID warehouseId,
        @NotNull(message = "Tipo é obrigatório") ManualMovementType type,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.000001", message = "Quantidade deve ser maior que zero") BigDecimal quantity
) {
}
