package com.hlima.erp.logistics.service;

import com.hlima.erp.logistics.dto.VehicleRequest;
import com.hlima.erp.logistics.dto.VehicleResponse;
import com.hlima.erp.logistics.entity.Vehicle;
import com.hlima.erp.logistics.mapper.VehicleMapper;
import com.hlima.erp.logistics.repository.VehicleRepository;
import com.hlima.erp.shared.exception.ConflictException;
import com.hlima.erp.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper mapper;

    public VehicleService(VehicleRepository vehicleRepository, VehicleMapper mapper) {
        this.vehicleRepository = vehicleRepository;
        this.mapper = mapper;
    }

    public List<VehicleResponse> list(boolean includeInactive) {
        List<Vehicle> vehicles = includeInactive ? vehicleRepository.findAll() : vehicleRepository.findByActiveTrue();
        return vehicles.stream().map(mapper::toResponse).toList();
    }

    public Vehicle getOrThrow(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Veículo"));
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        if (vehicleRepository.existsByPlate(request.plate())) {
            throw new ConflictException("Já existe um veículo com a placa " + request.plate());
        }

        Vehicle vehicle = new Vehicle(request.plate(), request.model(), request.capacityKg());
        return mapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void deactivate(UUID id) {
        getOrThrow(id).setActive(false);
    }
}
