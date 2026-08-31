package com.hlima.erp.catalog.repository;

import com.hlima.erp.catalog.entity.BillOfMaterial;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, UUID> {

    @EntityGraph(attributePaths = {"items", "items.ingredient", "product"})
    Optional<BillOfMaterial> findByProductId(UUID productId);
}
