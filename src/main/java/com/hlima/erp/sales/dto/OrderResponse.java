package com.hlima.erp.sales.dto;

import com.hlima.erp.sales.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        Long orderNumber,
        UUID customerId,
        String customerName,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        Instant createdAt
) {
}
