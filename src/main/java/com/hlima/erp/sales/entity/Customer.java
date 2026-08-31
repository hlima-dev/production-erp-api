package com.hlima.erp.sales.entity;

import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String document;

    @Column(nullable = false)
    private String name;

    private String email;

    private String phone;

    private String address;

    @Column(nullable = false)
    private boolean active = true;

    public Customer(String document, String name, String email, String phone, String address) {
        this.document = document;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}
