package com.hlima.erp.fiscal.repository;

import com.hlima.erp.fiscal.entity.Invoice;
import com.hlima.erp.fiscal.entity.InvoiceStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    boolean existsByOrderId(UUID orderId);

    @EntityGraph(attributePaths = {"order", "order.customer", "items", "items.product"})
    Optional<Invoice> findWithItemsById(UUID id);

    @EntityGraph(attributePaths = {"order", "order.customer"})
    List<Invoice> findByStatus(InvoiceStatus status);

    @EntityGraph(attributePaths = {"order", "order.customer"})
    List<Invoice> findAllBy();

    @Query(value = "select nextval('invoice_number_seq')", nativeQuery = true)
    Long nextInvoiceNumber();
}
