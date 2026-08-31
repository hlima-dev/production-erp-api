package com.hlima.erp.catalog.entity;

import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UnitOfMeasure unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType type;

    private String category;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    public Product(String code, String name, UnitOfMeasure unit, ProductType type, String category, BigDecimal price) {
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.type = type;
        this.category = category;
        this.price = price;
    }
}
