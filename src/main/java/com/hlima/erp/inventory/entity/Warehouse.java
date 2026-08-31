package com.hlima.erp.inventory.entity;

import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
public class Warehouse extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    public Warehouse(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
