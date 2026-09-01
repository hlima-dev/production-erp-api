package com.hlima.erp.fiscal.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItemResponse(
        UUID productId,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String cfop,
        BigDecimal icmsRate,
        BigDecimal icmsAmount,
        BigDecimal pisRate,
        BigDecimal pisAmount,
        BigDecimal cofinsRate,
        BigDecimal cofinsAmount
) {
}
