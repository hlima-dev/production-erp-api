package com.hlima.erp.inventory.mapper;

import com.hlima.erp.inventory.dto.StockItemResponse;
import com.hlima.erp.inventory.dto.StockMovementResponse;
import com.hlima.erp.inventory.entity.StockItem;
import com.hlima.erp.inventory.entity.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    StockItemResponse toResponse(StockItem stockItem);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    StockMovementResponse toResponse(StockMovement movement);
}
