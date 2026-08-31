package com.hlima.erp.production.service;

import com.hlima.erp.catalog.entity.BillOfMaterial;
import com.hlima.erp.catalog.entity.BillOfMaterialItem;
import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.catalog.entity.ProductType;
import com.hlima.erp.catalog.service.BillOfMaterialService;
import com.hlima.erp.catalog.service.ProductService;
import com.hlima.erp.inventory.entity.MovementType;
import com.hlima.erp.inventory.entity.Warehouse;
import com.hlima.erp.inventory.service.StockService;
import com.hlima.erp.inventory.service.WarehouseService;
import com.hlima.erp.production.dto.CompleteProductionRequest;
import com.hlima.erp.production.dto.ProductionOrderRequest;
import com.hlima.erp.production.dto.ProductionOrderResponse;
import com.hlima.erp.production.entity.ProductionOrder;
import com.hlima.erp.production.entity.ProductionOrderStatus;
import com.hlima.erp.production.mapper.ProductionOrderMapper;
import com.hlima.erp.production.repository.ProductionOrderRepository;
import com.hlima.erp.shared.exception.BusinessException;
import com.hlima.erp.shared.exception.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ordem de produção: ao iniciar, retira os insumos do almoxarifado conforme
 * a ficha técnica (BillOfMaterial) do produto × quantidade planejada; ao
 * concluir, dá entrada do produto acabado. Toda alteração de estoque passa
 * pelo StockService — este serviço nunca mexe em StockItem diretamente.
 */
@Service
@Transactional(readOnly = true)
public class ProductionOrderService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final BillOfMaterialService billOfMaterialService;
    private final StockService stockService;
    private final ProductionOrderMapper mapper;

    public ProductionOrderService(
            ProductionOrderRepository productionOrderRepository,
            ProductService productService,
            WarehouseService warehouseService,
            BillOfMaterialService billOfMaterialService,
            StockService stockService,
            ProductionOrderMapper mapper
    ) {
        this.productionOrderRepository = productionOrderRepository;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.billOfMaterialService = billOfMaterialService;
        this.stockService = stockService;
        this.mapper = mapper;
    }

    public List<ProductionOrderResponse> list(ProductionOrderStatus status) {
        List<ProductionOrder> orders = status == null
                ? productionOrderRepository.findAllBy()
                : productionOrderRepository.findByStatus(status);
        return orders.stream().map(mapper::toResponse).toList();
    }

    public ProductionOrderResponse findById(UUID id) {
        return mapper.toResponse(getOrThrow(id));
    }

    public ProductionOrder getOrThrow(UUID id) {
        return productionOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de produção"));
    }

    @Transactional
    public ProductionOrderResponse create(ProductionOrderRequest request) {
        Product product = productService.getOrThrow(request.productId());
        if (product.getType() != ProductType.PRODUTO_ACABADO) {
            throw new BusinessException(
                    "Só é possível abrir ordem de produção pra um produto acabado (" + product.getName() + " é matéria-prima).");
        }
        Warehouse warehouse = warehouseService.getOrThrow(request.warehouseId());

        ProductionOrder order = new ProductionOrder(
                productionOrderRepository.nextOrderNumber(), product, warehouse, request.plannedQuantity(), request.notes());

        return mapper.toResponse(productionOrderRepository.saveAndFlush(order));
    }

    /**
     * Retira do almoxarifado os insumos calculados pela ficha técnica ×
     * quantidade planejada. Valida o saldo de TODOS os insumos antes de
     * baixar qualquer um (mensagem de erro agregada), em vez de falhar no
     * meio da lista.
     */
    @Transactional
    public ProductionOrderResponse start(UUID id) {
        ProductionOrder order = getOrThrow(id);
        requireStatus(order, "iniciar", ProductionOrderStatus.ABERTA);

        BillOfMaterial bom = billOfMaterialService.findEntityByProductId(order.getProduct().getId())
                .orElseThrow(() -> new BusinessException(
                        "Produto " + order.getProduct().getName() + " não tem ficha técnica cadastrada."));

        if (bom.getItems().isEmpty()) {
            throw new BusinessException("Ficha técnica de " + order.getProduct().getName() + " não tem insumos cadastrados.");
        }

        UUID warehouseId = order.getWarehouse().getId();
        List<String> shortages = bom.getItems().stream()
                .filter(item -> requiredQuantity(item, order.getPlannedQuantity())
                        .compareTo(stockService.getAvailableQuantity(item.getIngredient().getId(), warehouseId)) > 0)
                .map(item -> item.getIngredient().getName())
                .collect(Collectors.toList());

        if (!shortages.isEmpty()) {
            throw new BusinessException("Estoque insuficiente pra iniciar a produção. Insumos em falta: " + String.join(", ", shortages) + ".");
        }

        for (BillOfMaterialItem item : bom.getItems()) {
            stockService.applyMovement(
                    item.getIngredient().getId(),
                    warehouseId,
                    MovementType.RETIRADA_PRODUCAO,
                    requiredQuantity(item, order.getPlannedQuantity()),
                    "PRODUCTION_ORDER",
                    order.getId());
        }

        order.setStatus(ProductionOrderStatus.EM_PRODUCAO);
        order.setStartedAt(Instant.now());

        return mapper.toResponse(order);
    }

    /** Dá entrada do produto acabado no estoque — quantidade produzida, se informada, senão a planejada. */
    @Transactional
    public ProductionOrderResponse complete(UUID id, CompleteProductionRequest request) {
        ProductionOrder order = getOrThrow(id);
        requireStatus(order, "concluir", ProductionOrderStatus.EM_PRODUCAO);

        BigDecimal produced = request != null && request.producedQuantity() != null
                ? request.producedQuantity()
                : order.getPlannedQuantity();

        stockService.applyMovement(
                order.getProduct().getId(),
                order.getWarehouse().getId(),
                MovementType.ENTRADA,
                produced,
                "PRODUCTION_ORDER",
                order.getId());

        order.setProducedQuantity(produced);
        order.setStatus(ProductionOrderStatus.CONCLUIDA);
        order.setCompletedAt(Instant.now());

        return mapper.toResponse(order);
    }

    /**
     * Cancelamento só é permitido enquanto a OP ainda está ABERTA (nenhum
     * insumo foi retirado do estoque ainda). Uma vez EM_PRODUCAO, cancelar
     * exigiria estornar as retiradas já feitas — deixado fora do escopo
     * deste projeto de portfólio; nesse caso a OP precisa ser concluída ou
     * tratada manualmente via lançamento de estoque.
     */
    @Transactional
    public ProductionOrderResponse cancel(UUID id) {
        ProductionOrder order = getOrThrow(id);
        requireStatus(order, "cancelar", ProductionOrderStatus.ABERTA);
        order.setStatus(ProductionOrderStatus.CANCELADA);
        return mapper.toResponse(order);
    }

    private BigDecimal requiredQuantity(BillOfMaterialItem item, BigDecimal plannedQuantity) {
        return item.getQuantityPerUnit().multiply(plannedQuantity);
    }

    private void requireStatus(ProductionOrder order, String action, ProductionOrderStatus expected) {
        if (order.getStatus() != expected) {
            throw new BusinessException(
                    "Não é possível " + action + " a ordem de produção: status atual é " + order.getStatus()
                            + ", esperado " + expected + ".");
        }
    }
}
