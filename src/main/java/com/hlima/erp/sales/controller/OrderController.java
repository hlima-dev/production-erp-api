package com.hlima.erp.sales.controller;

import com.hlima.erp.sales.dto.OrderRequest;
import com.hlima.erp.sales.dto.OrderResponse;
import com.hlima.erp.sales.entity.OrderStatus;
import com.hlima.erp.sales.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@Tag(name = "Pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> list(@RequestParam(required = false) OrderStatus status) {
        return orderService.list(status);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable UUID id) {
        return orderService.findById(id);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable UUID id, @Valid @RequestBody OrderRequest request) {
        return orderService.update(id, request);
    }

    @PostMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable UUID id) {
        return orderService.confirm(id);
    }

    @PostMapping("/{id}/separation")
    public OrderResponse startSeparation(@PathVariable UUID id) {
        return orderService.startSeparation(id);
    }

    @PostMapping("/{id}/deliver")
    public OrderResponse deliver(@PathVariable UUID id) {
        return orderService.markDelivered(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable UUID id) {
        return orderService.cancel(id);
    }
}
