package com.hlima.erp.catalog.entity;

public enum ProductType {
    MATERIA_PRIMA,
    // Passa por uma ordem de produção própria (tem ficha técnica, retira
    // insumos, dá entrada em estoque) e também pode ser insumo da ficha
    // técnica de outro produto — permite BOM em múltiplos níveis (ex: massa
    // de pizza é semi-acabado: produzida a partir de farinha/fermento/sal, e
    // depois consumida como insumo pra produzir a mini pizza de queijo).
    SEMI_ACABADO,
    PRODUTO_ACABADO
}
