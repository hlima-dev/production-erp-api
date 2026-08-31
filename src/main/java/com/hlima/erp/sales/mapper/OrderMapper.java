package com.hlima.erp.sales.mapper;

import com.hlima.erp.sales.dto.OrderItemResponse;
import com.hlima.erp.sales.dto.OrderResponse;
import com.hlima.erp.sales.entity.Order;
import com.hlima.erp.sales.entity.OrderItem;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderItemResponse toResponse(OrderItem item);
}
