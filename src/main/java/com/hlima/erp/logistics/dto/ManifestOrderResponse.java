package com.hlima.erp.logistics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ManifestOrderResponse(
        UUID orderId,
        Long orderNumber,
        String customerName,
        BigDecimal totalAmount
) {
}
