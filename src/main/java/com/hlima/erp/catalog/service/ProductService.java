package com.hlima.erp.catalog.service;

import com.hlima.erp.catalog.dto.ProductRequest;
import com.hlima.erp.catalog.dto.ProductResponse;
import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.catalog.entity.ProductType;
import com.hlima.erp.catalog.mapper.ProductMapper;
import com.hlima.erp.catalog.repository.ProductRepository;
import com.hlima.erp.shared.exception.ConflictException;
import com.hlima.erp.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductResponse> list(ProductType type, boolean includeInactive) {
        List<Product> products = includeInactive ? productRepository.findAll() : productRepository.findByActiveTrue();

        return products.stream()
                .filter(p -> type == null || p.getType() == type)
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse findById(UUID id) {
        return productMapper.toResponse(getOrThrow(id));
    }

    public Product getOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto"));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByCode(request.code())) {
            throw new ConflictException("Já existe um produto com o código " + request.code());
        }

        Product product = new Product(
                request.code(),
                request.name(),
                request.unit(),
                request.type(),
                request.category(),
                request.price()
        );

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getOrThrow(id);

        if (!product.getCode().equals(request.code()) && productRepository.existsByCode(request.code())) {
            throw new ConflictException("Já existe um produto com o código " + request.code());
        }

        product.setCode(request.code());
        product.setName(request.name());
        product.setUnit(request.unit());
        product.setType(request.type());
        product.setCategory(request.category());
        product.setPrice(request.price());

        return productMapper.toResponse(product);
    }

    @Transactional
    public void deactivate(UUID id) {
        Product product = getOrThrow(id);
        product.setActive(false);
    }
}
