package com.hlima.erp.logistics.repository;

import com.hlima.erp.logistics.entity.Vehicle;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByPlate(String plate);

    List<Vehicle> findByActiveTrue();
}
