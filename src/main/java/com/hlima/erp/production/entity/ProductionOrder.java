package com.hlima.erp.production.entity;

import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.inventory.entity.Warehouse;
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
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ordem de produção (OP): reproduz a tela "Retirada (Controle de Produção)"
 * dos prints — ao iniciar, calcula os insumos necessários a partir da
 * BillOfMaterial do produto × quantidade planejada e baixa o estoque deles;
 * ao concluir, dá entrada do produto acabado.
 */
@Entity
@Table(name = "production_orders")
@Getter
@Setter
@NoArgsConstructor
public class ProductionOrder extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true)
    private Long orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "planned_quantity", nullable = false, precision = 14, scale = 6)
    private BigDecimal plannedQuantity;

    @Column(name = "produced_quantity", precision = 14, scale = 6)
    private BigDecimal producedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductionOrderStatus status = ProductionOrderStatus.ABERTA;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(length = 500)
    private String notes;

    public ProductionOrder(Long orderNumber, Product product, Warehouse warehouse, BigDecimal plannedQuantity, String notes) {
        this.orderNumber = orderNumber;
        this.product = product;
        this.warehouse = warehouse;
        this.plannedQuantity = plannedQuantity;
        this.notes = notes;
    }
}
