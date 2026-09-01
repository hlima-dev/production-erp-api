package com.hlima.erp.fiscal.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InvoiceRequest(
        @NotNull(message = "Pedido é obrigatório") UUID orderId
) {
}
