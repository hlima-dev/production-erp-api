package com.hlima.erp.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record BillOfMaterialItemRequest(
        @NotNull(message = "Insumo é obrigatório") UUID ingredientId,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.000001", message = "Quantidade deve ser maior que zero") BigDecimal quantityPerUnit
) {
}
