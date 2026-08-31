package com.hlima.erp.catalog.dto;

import com.hlima.erp.catalog.entity.ProductType;
import com.hlima.erp.catalog.entity.UnitOfMeasure;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String code,
        String name,
        UnitOfMeasure unit,
        ProductType type,
        String category,
        BigDecimal price,
        boolean active
) {
}
