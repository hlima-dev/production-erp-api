package com.hlima.erp.fiscal.mapper;

import com.hlima.erp.fiscal.dto.InvoiceItemResponse;
import com.hlima.erp.fiscal.dto.InvoiceResponse;
import com.hlima.erp.fiscal.entity.Invoice;
import com.hlima.erp.fiscal.entity.InvoiceItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "customerName", source = "order.customer.name")
    @Mapping(target = "issueDate", source = "createdAt")
    InvoiceResponse toResponse(Invoice invoice);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    InvoiceItemResponse toResponse(InvoiceItem item);
}
