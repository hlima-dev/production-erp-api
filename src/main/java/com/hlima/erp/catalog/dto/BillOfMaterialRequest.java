package com.hlima.erp.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BillOfMaterialRequest(
        String notes,
        @NotEmpty(message = "A ficha técnica precisa de ao menos um insumo")
        @Valid List<BillOfMaterialItemRequest> items
) {
}
