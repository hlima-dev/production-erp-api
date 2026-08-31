package com.hlima.erp.sales.entity;

/**
 * Fluxo linear do pedido: RASCUNHO → CONFIRMADO → EM_SEPARACAO → FATURADO →
 * EXPEDIDO → ENTREGUE. FATURADO é setado pelo módulo fiscal ao emitir a
 * NF-e; EXPEDIDO é setado pelo módulo logistics ao incluir o pedido num
 * romaneio. CANCELADO é possível até EM_SEPARACAO (depois disso, com nota
 * fiscal já emitida, o cancelamento exigiria uma NF-e de cancelamento —
 * fora do escopo simulado deste projeto).
 */
public enum OrderStatus {
    RASCUNHO,
    CONFIRMADO,
    EM_SEPARACAO,
    FATURADO,
    EXPEDIDO,
    ENTREGUE,
    CANCELADO
}
