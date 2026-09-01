package com.hlima.erp.logistics.mapper;

import com.hlima.erp.logistics.dto.DeliveryManifestResponse;
import com.hlima.erp.logistics.dto.ManifestOrderResponse;
import com.hlima.erp.logistics.entity.DeliveryManifest;
import com.hlima.erp.sales.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryManifestMapper {

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehiclePlate", source = "vehicle.plate")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.name")
    DeliveryManifestResponse toResponse(DeliveryManifest manifest);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "orderNumber", source = "orderNumber")
    @Mapping(target = "customerName", source = "customer.name")
    ManifestOrderResponse toOrderResponse(Order order);
}
