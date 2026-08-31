package com.hlima.erp.inventory.controller;

import com.hlima.erp.inventory.dto.StockItemResponse;
import com.hlima.erp.inventory.dto.StockMovementRequest;
import com.hlima.erp.inventory.dto.StockMovementResponse;
import com.hlima.erp.inventory.service.StockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@Tag(name = "Estoque")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/stock")
    public List<StockItemResponse> listBalances() {
        return stockService.listBalances();
    }

    @PostMapping("/movements")
    public ResponseEntity<StockMovementResponse> registerMovement(@Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.registerManualMovement(request));
    }
}
