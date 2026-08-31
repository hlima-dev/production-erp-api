package com.hlima.erp.sales.repository;

import com.hlima.erp.sales.entity.Customer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByDocument(String document);

    List<Customer> findByActiveTrue();
}
