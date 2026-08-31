package com.hlima.erp.inventory.mapper;

import com.hlima.erp.inventory.dto.WarehouseResponse;
import com.hlima.erp.inventory.entity.Warehouse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    WarehouseResponse toResponse(Warehouse warehouse);
}
