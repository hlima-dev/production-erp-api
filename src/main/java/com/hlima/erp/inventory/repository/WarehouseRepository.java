package com.hlima.erp.inventory.repository;

import com.hlima.erp.inventory.entity.Warehouse;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    boolean existsByCode(String code);
}
