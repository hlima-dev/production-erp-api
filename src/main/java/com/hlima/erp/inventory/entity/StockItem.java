package com.hlima.erp.inventory.entity;

import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_items", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"}))
@Getter
@Setter
@NoArgsConstructor
public class StockItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, precision = 14, scale = 6)
    private BigDecimal quantity = BigDecimal.ZERO;

    public StockItem(Product product, Warehouse warehouse) {
        this.product = product;
        this.warehouse = warehouse;
    }
}
