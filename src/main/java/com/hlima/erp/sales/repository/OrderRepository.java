package com.hlima.erp.sales.repository;

import com.hlima.erp.sales.entity.Order;
import com.hlima.erp.sales.entity.OrderStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    Optional<Order> findWithItemsById(UUID id);

    @EntityGraph(attributePaths = {"customer"})
    List<Order> findByStatus(OrderStatus status);

    @EntityGraph(attributePaths = {"customer"})
    List<Order> findAllBy();

    // Sequência dedicada (order_number_seq, criada na V4) — gera o próximo
    // número sequencial do pedido de forma atômica no banco, sem risco de
    // colisão em requisições concorrentes.
    @Query(value = "select nextval('order_number_seq')", nativeQuery = true)
    Long nextOrderNumber();
}
