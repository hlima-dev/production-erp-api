package com.hlima.erp.catalog.mapper;

import com.hlima.erp.catalog.dto.ProductResponse;
import com.hlima.erp.catalog.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);
}
