package com.hlima.erp.catalog.dto;

import com.hlima.erp.catalog.entity.ProductType;
import com.hlima.erp.catalog.entity.UnitOfMeasure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Código é obrigatório") String code,
        @NotBlank(message = "Nome é obrigatório") String name,
        @NotNull(message = "Unidade é obrigatória") UnitOfMeasure unit,
        @NotNull(message = "Tipo é obrigatório") ProductType type,
        String category,
        @DecimalMin(value = "0.0", inclusive = true, message = "Preço não pode ser negativo") BigDecimal price
) {
}
