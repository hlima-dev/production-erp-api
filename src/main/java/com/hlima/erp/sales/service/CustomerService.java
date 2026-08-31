package com.hlima.erp.sales.service;

import com.hlima.erp.sales.dto.CustomerRequest;
import com.hlima.erp.sales.dto.CustomerResponse;
import com.hlima.erp.sales.entity.Customer;
import com.hlima.erp.sales.mapper.CustomerMapper;
import com.hlima.erp.sales.repository.CustomerRepository;
import com.hlima.erp.shared.exception.ConflictException;
import com.hlima.erp.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    public List<CustomerResponse> list(boolean includeInactive) {
        List<Customer> customers = includeInactive ? customerRepository.findAll() : customerRepository.findByActiveTrue();
        return customers.stream().map(mapper::toResponse).toList();
    }

    public CustomerResponse findById(UUID id) {
        return mapper.toResponse(getOrThrow(id));
    }

    public Customer getOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente"));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByDocument(request.document())) {
            throw new ConflictException("Já existe um cliente com o documento " + request.document());
        }

        Customer customer = new Customer(
                request.document(), request.name(), request.email(), request.phone(), request.address());
        return mapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = getOrThrow(id);

        if (!customer.getDocument().equals(request.document()) && customerRepository.existsByDocument(request.document())) {
            throw new ConflictException("Já existe um cliente com o documento " + request.document());
        }

        customer.setDocument(request.document());
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());

        return mapper.toResponse(customer);
    }

    @Transactional
    public void deactivate(UUID id) {
        getOrThrow(id).setActive(false);
    }
}
