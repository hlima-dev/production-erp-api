package com.hlima.erp.catalog.entity;

import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ficha técnica de um produto acabado — quanto de cada insumo é necessário
 * pra produzir 1 unidade dele. Um produto acabado tem no máximo uma BOM.
 */
@Entity
@Table(name = "bill_of_materials")
@Getter
@Setter
@NoArgsConstructor
public class BillOfMaterial extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "billOfMaterial", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BillOfMaterialItem> items = new ArrayList<>();

    public BillOfMaterial(Product product) {
        this.product = product;
    }

    public void replaceItems(List<BillOfMaterialItem> newItems) {
        items.clear();
        newItems.forEach(item -> item.setBillOfMaterial(this));
        items.addAll(newItems);
    }
}
