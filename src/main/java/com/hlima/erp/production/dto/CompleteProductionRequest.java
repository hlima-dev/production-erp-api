package com.hlima.erp.production.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * Quantidade realmente produzida, se diferente da planejada (ex: perda no
 * processo). Se não informada, usa a quantidade planejada da OP.
 */
public record CompleteProductionRequest(
        @DecimalMin(value = "0.000001", message = "Quantidade produzida deve ser maior que zero") BigDecimal producedQuantity
) {
}
