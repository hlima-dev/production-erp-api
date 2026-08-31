package com.hlima.erp.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record OrderRequest(
        @NotNull(message = "Cliente é obrigatório") UUID customerId,
        @NotEmpty(message = "Pedido precisa ter ao menos um item")
        @Valid List<OrderItemRequest> items
) {
}
