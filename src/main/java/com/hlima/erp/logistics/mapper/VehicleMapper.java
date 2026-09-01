package com.hlima.erp.logistics.mapper;

import com.hlima.erp.logistics.dto.VehicleResponse;
import com.hlima.erp.logistics.entity.Vehicle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    VehicleResponse toResponse(Vehicle vehicle);
}
