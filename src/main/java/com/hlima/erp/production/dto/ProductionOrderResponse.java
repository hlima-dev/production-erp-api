package com.hlima.erp.production.dto;

import com.hlima.erp.production.entity.ProductionOrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductionOrderResponse(
        UUID id,
        Long orderNumber,
        UUID productId,
        String productName,
        UUID warehouseId,
        String warehouseName,
        BigDecimal plannedQuantity,
        BigDecimal producedQuantity,
        ProductionOrderStatus status,
        Instant startedAt,
        Instant completedAt,
        String notes,
        Instant createdAt
) {
}
