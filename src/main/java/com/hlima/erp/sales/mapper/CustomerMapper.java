package com.hlima.erp.sales.mapper;

import com.hlima.erp.sales.dto.CustomerResponse;
import com.hlima.erp.sales.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);
}
