package com.hlima.erp.logistics.entity;

import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
public class Vehicle extends BaseEntity {

    @Column(nullable = false, unique = true, length = 10)
    private String plate;

    @Column(nullable = false)
    private String model;

    // Capacidade de carga em kg — opcional, só informativo aqui.
    @Column(precision = 10, scale = 2)
    private BigDecimal capacityKg;

    @Column(nullable = false)
    private boolean active = true;

    public Vehicle(String plate, String model, BigDecimal capacityKg) {
        this.plate = plate;
        this.model = model;
        this.capacityKg = capacityKg;
    }
}
