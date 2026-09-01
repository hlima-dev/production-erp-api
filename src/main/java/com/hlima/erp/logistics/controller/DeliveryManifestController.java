package com.hlima.erp.logistics.controller;

import com.hlima.erp.logistics.dto.DeliveryManifestRequest;
import com.hlima.erp.logistics.dto.DeliveryManifestResponse;
import com.hlima.erp.logistics.entity.DeliveryManifestStatus;
import com.hlima.erp.logistics.service.DeliveryManifestService;
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
@RequestMapping("/delivery-manifests")
@Tag(name = "Expedição")
public class DeliveryManifestController {

    private final DeliveryManifestService deliveryManifestService;

    public DeliveryManifestController(DeliveryManifestService deliveryManifestService) {
        this.deliveryManifestService = deliveryManifestService;
    }

    @GetMapping
    public List<DeliveryManifestResponse> list(@RequestParam(required = false) DeliveryManifestStatus status) {
        return deliveryManifestService.list(status);
    }

    @GetMapping("/{id}")
    public DeliveryManifestResponse findById(@PathVariable UUID id) {
        return deliveryManifestService.findById(id);
    }

    @PostMapping
    public ResponseEntity<DeliveryManifestResponse> create(@Valid @RequestBody DeliveryManifestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryManifestService.create(request));
    }

    @PostMapping("/{id}/start")
    public DeliveryManifestResponse start(@PathVariable UUID id) {
        return deliveryManifestService.start(id);
    }

    @PostMapping("/{id}/complete")
    public DeliveryManifestResponse complete(@PathVariable UUID id) {
        return deliveryManifestService.complete(id);
    }
}
