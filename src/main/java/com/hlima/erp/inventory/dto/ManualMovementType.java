package com.hlima.erp.inventory.dto;

// RETIRADA_PRODUCAO (o outro valor de MovementType) só é gerada
// internamente pelo módulo de produção — não faz sentido lançar isso à
// mão, por isso o request de movimentação manual só aceita estes dois.
public enum ManualMovementType {
    ENTRADA,
    SAIDA
}
