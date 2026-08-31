package com.hlima.erp.catalog.repository;

import com.hlima.erp.catalog.entity.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByCode(String code);

    boolean existsByCode(String code);

    List<Product> findByActiveTrue();
}
