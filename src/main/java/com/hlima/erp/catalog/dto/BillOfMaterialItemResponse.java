package com.hlima.erp.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BillOfMaterialItemResponse(
        UUID ingredientId,
        String ingredientCode,
        String ingredientName,
        BigDecimal quantityPerUnit
) {
}
