package com.hlima.erp.inventory.repository;

import com.hlima.erp.inventory.entity.StockMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByProductIdOrderByCreatedAtDesc(UUID productId);

    List<StockMovement> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);
}
