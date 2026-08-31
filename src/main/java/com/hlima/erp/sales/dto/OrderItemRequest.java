package com.hlima.erp.sales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequest(
        @NotNull(message = "Produto é obrigatório") UUID productId,
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.000001", message = "Quantidade deve ser maior que zero") BigDecimal quantity,
        // Opcional: se não informado, usa o preço cadastrado no produto.
        @DecimalMin(value = "0.00", message = "Preço não pode ser negativo") BigDecimal unitPrice
) {
}
