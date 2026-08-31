package com.hlima.erp.production.repository;

import com.hlima.erp.production.entity.ProductionOrder;
import com.hlima.erp.production.entity.ProductionOrderStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, UUID> {

    @EntityGraph(attributePaths = {"product", "warehouse"})
    List<ProductionOrder> findByStatus(ProductionOrderStatus status);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    List<ProductionOrder> findAllBy();

    @Query(value = "select nextval('production_order_number_seq')", nativeQuery = true)
    Long nextOrderNumber();
}
