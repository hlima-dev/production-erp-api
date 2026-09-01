package com.hlima.erp.logistics.mapper;

import com.hlima.erp.logistics.dto.DriverResponse;
import com.hlima.erp.logistics.entity.Driver;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    DriverResponse toResponse(Driver driver);
}
