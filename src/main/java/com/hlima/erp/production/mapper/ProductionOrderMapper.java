package com.hlima.erp.production.mapper;

import com.hlima.erp.production.dto.ProductionOrderResponse;
import com.hlima.erp.production.entity.ProductionOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductionOrderMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    ProductionOrderResponse toResponse(ProductionOrder order);
}
