package com.hlima.erp.inventory.entity;

import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Histórico de toda alteração de saldo — nunca se atualiza a quantidade
 * de um StockItem diretamente sem gerar um registro aqui.
 */
@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Column(nullable = false, precision = 14, scale = 6)
    private BigDecimal quantity;

    // De onde veio o movimento (ex: "PRODUCTION_ORDER") e o id de referência,
    // pra rastrear qual ordem de produção/pedido gerou essa baixa/entrada.
    @Column(length = 40)
    private String referenceType;

    private UUID referenceId;

    public StockMovement(Product product, Warehouse warehouse, MovementType type, BigDecimal quantity,
                          String referenceType, UUID referenceId) {
        this.product = product;
        this.warehouse = warehouse;
        this.type = type;
        this.quantity = quantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }
}
