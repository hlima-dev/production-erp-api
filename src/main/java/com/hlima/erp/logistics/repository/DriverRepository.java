package com.hlima.erp.logistics.repository;

import com.hlima.erp.logistics.entity.Driver;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    boolean existsByDocument(String document);

    boolean existsByLicense(String license);

    List<Driver> findByActiveTrue();
}
