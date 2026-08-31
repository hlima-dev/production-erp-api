package com.hlima.erp.catalog.mapper;

import com.hlima.erp.catalog.dto.BillOfMaterialItemResponse;
import com.hlima.erp.catalog.dto.BillOfMaterialResponse;
import com.hlima.erp.catalog.entity.BillOfMaterial;
import com.hlima.erp.catalog.entity.BillOfMaterialItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BillOfMaterialMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    BillOfMaterialResponse toResponse(BillOfMaterial bom);

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientCode", source = "ingredient.code")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    BillOfMaterialItemResponse toItemResponse(BillOfMaterialItem item);
}
