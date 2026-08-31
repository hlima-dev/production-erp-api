package com.hlima.erp.sales.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank(message = "Documento (CPF/CNPJ) é obrigatório") String document,
        @NotBlank(message = "Nome é obrigatório") String name,
        @Email(message = "E-mail inválido") String email,
        String phone,
        String address
) {
}
