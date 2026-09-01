package com.hlima.erp.logistics.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record DeliveryManifestRequest(
        @NotNull(message = "Veículo é obrigatório") UUID vehicleId,
        @NotNull(message = "Motorista é obrigatório") UUID driverId,
        @NotEmpty(message = "Romaneio precisa ter ao menos um pedido") List<UUID> orderIds,
        String notes
) {
}
