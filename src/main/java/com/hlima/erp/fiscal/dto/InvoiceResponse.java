package com.hlima.erp.fiscal.dto;

import com.hlima.erp.fiscal.entity.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        Long invoiceNumber,
        String accessKey,
        UUID orderId,
        Long orderNumber,
        String customerName,
        InvoiceStatus status,
        BigDecimal totalAmount,
        BigDecimal totalIcms,
        BigDecimal totalPis,
        BigDecimal totalCofins,
        List<InvoiceItemResponse> items,
        Instant issueDate
) {
}
