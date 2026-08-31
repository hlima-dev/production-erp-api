package com.hlima.erp.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockItemResponse(
        UUID productId,
        String productCode,
        String productName,
        UUID warehouseId,
        String warehouseName,
        BigDecimal quantity
) {
}
