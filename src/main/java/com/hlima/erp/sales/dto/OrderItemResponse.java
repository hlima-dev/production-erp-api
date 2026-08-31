package com.hlima.erp.sales.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
