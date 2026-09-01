package com.hlima.erp.fiscal.controller;

import com.hlima.erp.fiscal.dto.InvoiceRequest;
import com.hlima.erp.fiscal.dto.InvoiceResponse;
import com.hlima.erp.fiscal.entity.InvoiceStatus;
import com.hlima.erp.fiscal.service.InvoiceService;
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

/** NF-e SIMULADA — ver Javadoc de {@link InvoiceService}. */
@RestController
@RequestMapping("/invoices")
@Tag(name = "Nota Fiscal (simulada)")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public List<InvoiceResponse> list(@RequestParam(required = false) InvoiceStatus status) {
        return invoiceService.list(status);
    }

    @GetMapping("/{id}")
    public InvoiceResponse findById(@PathVariable UUID id) {
        return invoiceService.findById(id);
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(request));
    }

    @PostMapping("/{id}/cancel")
    public InvoiceResponse cancel(@PathVariable UUID id) {
        return invoiceService.cancel(id);
    }
}
