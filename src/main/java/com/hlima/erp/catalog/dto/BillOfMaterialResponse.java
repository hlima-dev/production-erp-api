package com.hlima.erp.catalog.dto;

import java.util.List;
import java.util.UUID;

public record BillOfMaterialResponse(
        UUID id,
        UUID productId,
        String productName,
        String notes,
        List<BillOfMaterialItemResponse> items
) {
}
