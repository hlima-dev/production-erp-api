package com.hlima.erp.logistics.dto;

import jakarta.validation.constraints.NotBlank;

public record DriverRequest(
        @NotBlank(message = "Nome é obrigatório") String name,
        @NotBlank(message = "Documento (CPF) é obrigatório") String document,
        @NotBlank(message = "CNH é obrigatória") String license,
        String phone
) {
}
