package com.hlima.erp.inventory.repository;

import com.hlima.erp.inventory.entity.StockItem;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

    List<StockItem> findByProductId(UUID productId);

    Optional<StockItem> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);

    // Lock pessimista — duas ordens de produção baixando o mesmo insumo ao
    // mesmo tempo não podem ler o mesmo saldo "antigo" e as duas passarem
    // na validação de estoque suficiente.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockItem s where s.product.id = :productId and s.warehouse.id = :warehouseId")
    Optional<StockItem> findForUpdate(UUID productId, UUID warehouseId);
}
