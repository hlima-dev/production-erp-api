package com.hlima.erp.logistics.entity;

import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
public class Driver extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String document;

    // Número da CNH.
    @Column(nullable = false, unique = true, length = 20)
    private String license;

    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    public Driver(String name, String document, String license, String phone) {
        this.name = name;
        this.document = document;
        this.license = license;
        this.phone = phone;
    }
}
