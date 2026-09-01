package com.hlima.erp.logistics.service;

import com.hlima.erp.logistics.dto.DriverRequest;
import com.hlima.erp.logistics.dto.DriverResponse;
import com.hlima.erp.logistics.entity.Driver;
import com.hlima.erp.logistics.mapper.DriverMapper;
import com.hlima.erp.logistics.repository.DriverRepository;
import com.hlima.erp.shared.exception.ConflictException;
import com.hlima.erp.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper mapper;

    public DriverService(DriverRepository driverRepository, DriverMapper mapper) {
        this.driverRepository = driverRepository;
        this.mapper = mapper;
    }

    public List<DriverResponse> list(boolean includeInactive) {
        List<Driver> drivers = includeInactive ? driverRepository.findAll() : driverRepository.findByActiveTrue();
        return drivers.stream().map(mapper::toResponse).toList();
    }

    public Driver getOrThrow(UUID id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Motorista"));
    }

    @Transactional
    public DriverResponse create(DriverRequest request) {
        if (driverRepository.existsByDocument(request.document())) {
            throw new ConflictException("Já existe um motorista com o documento " + request.document());
        }
        if (driverRepository.existsByLicense(request.license())) {
            throw new ConflictException("Já existe um motorista com a CNH " + request.license());
        }

        Driver driver = new Driver(request.name(), request.document(), request.license(), request.phone());
        return mapper.toResponse(driverRepository.save(driver));
    }

    @Transactional
    public void deactivate(UUID id) {
        getOrThrow(id).setActive(false);
    }
}
