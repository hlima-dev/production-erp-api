package com.hlima.erp.inventory.service;

import com.hlima.erp.inventory.dto.WarehouseRequest;
import com.hlima.erp.inventory.dto.WarehouseResponse;
import com.hlima.erp.inventory.entity.Warehouse;
import com.hlima.erp.inventory.mapper.WarehouseMapper;
import com.hlima.erp.inventory.repository.WarehouseRepository;
import com.hlima.erp.shared.exception.ConflictException;
import com.hlima.erp.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper mapper;

    public WarehouseService(WarehouseRepository warehouseRepository, WarehouseMapper mapper) {
        this.warehouseRepository = warehouseRepository;
        this.mapper = mapper;
    }

    public List<WarehouseResponse> list() {
        return warehouseRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    public Warehouse getOrThrow(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Almoxarifado"));
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.code())) {
            throw new ConflictException("Já existe um almoxarifado com o código " + request.code());
        }

        return mapper.toResponse(warehouseRepository.save(new Warehouse(request.code(), request.name())));
    }
}
