package com.hlima.erp.production.controller;

import com.hlima.erp.production.dto.CompleteProductionRequest;
import com.hlima.erp.production.dto.ProductionOrderRequest;
import com.hlima.erp.production.dto.ProductionOrderResponse;
import com.hlima.erp.production.entity.ProductionOrderStatus;
import com.hlima.erp.production.service.ProductionOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/production-orders")
@Tag(name = "Ordens de Produção")
public class ProductionOrderController {

    private final ProductionOrderService productionOrderService;

    public ProductionOrderController(ProductionOrderService productionOrderService) {
        this.productionOrderService = productionOrderService;
    }

    @GetMapping
    public List<ProductionOrderResponse> list(@RequestParam(required = false) ProductionOrderStatus status) {
        return productionOrderService.list(status);
    }

    @GetMapping("/{id}")
    public ProductionOrderResponse findById(@PathVariable UUID id) {
        return productionOrderService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProductionOrderResponse> create(@Valid @RequestBody ProductionOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productionOrderService.create(request));
    }

    @PostMapping("/{id}/start")
    public ProductionOrderResponse start(@PathVariable UUID id) {
        return productionOrderService.start(id);
    }

    @PostMapping("/{id}/complete")
    public ProductionOrderResponse complete(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CompleteProductionRequest request
    ) {
        return productionOrderService.complete(id, request);
    }

    @PostMapping("/{id}/cancel")
    public ProductionOrderResponse cancel(@PathVariable UUID id) {
        return productionOrderService.cancel(id);
    }
}
