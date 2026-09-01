package com.hlima.erp.logistics.entity;

import com.hlima.erp.sales.entity.Order;
import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Romaneio de expedição: agrupa um ou mais pedidos faturados num veículo +
 * motorista. Um pedido só pode estar num romaneio por vez — garantido pela
 * constraint única em delivery_manifest_orders.order_id e checado em
 * DeliveryManifestService antes de incluir.
 */
@Entity
@Table(name = "delivery_manifests")
@Getter
@Setter
@NoArgsConstructor
public class DeliveryManifest extends BaseEntity {

    @Column(name = "manifest_number", nullable = false, unique = true)
    private Long manifestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryManifestStatus status = DeliveryManifestStatus.PLANEJADO;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "delivery_manifest_orders",
            joinColumns = @JoinColumn(name = "manifest_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    private Set<Order> orders = new HashSet<>();

    @Column(name = "departed_at")
    private Instant departedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(length = 500)
    private String notes;

    public DeliveryManifest(Long manifestNumber, Vehicle vehicle, Driver driver, String notes) {
        this.manifestNumber = manifestNumber;
        this.vehicle = vehicle;
        this.driver = driver;
        this.notes = notes;
    }
}
