package com.hlima.erp.catalog.entity;

import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bill_of_material_items")
@Getter
@Setter
@NoArgsConstructor
public class BillOfMaterialItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_of_material_id", nullable = false)
    private BillOfMaterial billOfMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Product ingredient;

    // Quantidade do insumo necessária pra produzir 1 unidade do produto acabado.
    @Column(nullable = false, precision = 14, scale = 6)
    private BigDecimal quantityPerUnit;

    public BillOfMaterialItem(Product ingredient, BigDecimal quantityPerUnit) {
        this.ingredient = ingredient;
        this.quantityPerUnit = quantityPerUnit;
    }
}
