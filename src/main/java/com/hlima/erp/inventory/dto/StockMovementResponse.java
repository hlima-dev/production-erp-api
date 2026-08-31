package com.hlima.erp.inventory.dto;

import com.hlima.erp.inventory.entity.MovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID productId,
        String productName,
        UUID warehouseId,
        String warehouseName,
        MovementType type,
        BigDecimal quantity,
        String referenceType,
        UUID referenceId,
        Instant createdAt
) {
}
