package com.hlima.erp.catalog.controller;

import com.hlima.erp.catalog.dto.BillOfMaterialRequest;
import com.hlima.erp.catalog.dto.BillOfMaterialResponse;
import com.hlima.erp.catalog.dto.ProductRequest;
import com.hlima.erp.catalog.dto.ProductResponse;
import com.hlima.erp.catalog.entity.ProductType;
import com.hlima.erp.catalog.service.BillOfMaterialService;
import com.hlima.erp.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@Tag(name = "Produtos e ficha técnica")
public class ProductController {

    private final ProductService productService;
    private final BillOfMaterialService billOfMaterialService;

    public ProductController(ProductService productService, BillOfMaterialService billOfMaterialService) {
        this.productService = productService;
        this.billOfMaterialService = billOfMaterialService;
    }

    @GetMapping
    public List<ProductResponse> list(
            @RequestParam(required = false) ProductType type,
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return productService.list(type, includeInactive);
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bom")
    public BillOfMaterialResponse getBillOfMaterial(@PathVariable UUID id) {
        return billOfMaterialService.getByProductId(id);
    }

    @PutMapping("/{id}/bom")
    @PreAuthorize("hasRole('ADMIN')")
    public BillOfMaterialResponse saveBillOfMaterial(@PathVariable UUID id, @Valid @RequestBody BillOfMaterialRequest request) {
        return billOfMaterialService.save(id, request);
    }
}
