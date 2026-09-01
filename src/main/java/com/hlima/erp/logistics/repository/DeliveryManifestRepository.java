package com.hlima.erp.logistics.repository;

import com.hlima.erp.logistics.entity.DeliveryManifest;
import com.hlima.erp.logistics.entity.DeliveryManifestStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryManifestRepository extends JpaRepository<DeliveryManifest, UUID> {

    @EntityGraph(attributePaths = {"vehicle", "driver", "orders", "orders.customer"})
    Optional<DeliveryManifest> findWithOrdersById(UUID id);

    @EntityGraph(attributePaths = {"vehicle", "driver"})
    List<DeliveryManifest> findByStatus(DeliveryManifestStatus status);

    @EntityGraph(attributePaths = {"vehicle", "driver"})
    List<DeliveryManifest> findAllBy();

    // Pra checar se um pedido já está em algum romaneio antes de incluir
    // (um pedido só pode ser expedido uma vez).
    boolean existsByOrders_Id(UUID orderId);

    @Query(value = "select nextval('delivery_manifest_number_seq')", nativeQuery = true)
    Long nextManifestNumber();
}
