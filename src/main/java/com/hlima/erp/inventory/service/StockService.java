package com.hlima.erp.inventory.service;

import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.catalog.service.ProductService;
import com.hlima.erp.inventory.dto.ManualMovementType;
import com.hlima.erp.inventory.dto.StockItemResponse;
import com.hlima.erp.inventory.dto.StockMovementRequest;
import com.hlima.erp.inventory.dto.StockMovementResponse;
import com.hlima.erp.inventory.entity.MovementType;
import com.hlima.erp.inventory.entity.StockItem;
import com.hlima.erp.inventory.entity.StockMovement;
import com.hlima.erp.inventory.entity.Warehouse;
import com.hlima.erp.inventory.mapper.StockMapper;
import com.hlima.erp.inventory.repository.StockItemRepository;
import com.hlima.erp.inventory.repository.StockMovementRepository;
import com.hlima.erp.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Único lugar que altera o saldo de um StockItem — sempre acompanhado de um
 * StockMovement, nunca um update direto de quantidade.
 */
@Service
@Transactional(readOnly = true)
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final StockMapper mapper;

    public StockService(
            StockItemRepository stockItemRepository,
            StockMovementRepository stockMovementRepository,
            ProductService productService,
            WarehouseService warehouseService,
            StockMapper mapper
    ) {
        this.stockItemRepository = stockItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.mapper = mapper;
    }

    public List<StockItemResponse> listBalances() {
        return stockItemRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    public BigDecimal getAvailableQuantity(UUID productId, UUID warehouseId) {
        return stockItemRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .map(StockItem::getQuantity)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public StockMovementResponse registerManualMovement(StockMovementRequest request) {
        MovementType type = request.type() == ManualMovementType.ENTRADA ? MovementType.ENTRADA : MovementType.SAIDA;
        StockMovement movement = applyMovement(
                request.productId(), request.warehouseId(), type, request.quantity(), null, null);
        return mapper.toResponse(movement);
    }

    /**
     * Ponto único de baixa/entrada de estoque, usado pelo lançamento manual
     * e pelo módulo de produção (retirada de insumos / entrada do produto
     * acabado). Lança BusinessException se for uma saída maior que o saldo
     * disponível.
     */
    @Transactional
    public StockMovement applyMovement(
            UUID productId, UUID warehouseId, MovementType type, BigDecimal quantity,
            String referenceType, UUID referenceId
    ) {
        Product product = productService.getOrThrow(productId);
        Warehouse warehouse = warehouseService.getOrThrow(warehouseId);

        // Nota: se for a primeiríssima movimentação desse produto nesse
        // almoxarifado, o findForUpdate ainda não tem linha pra travar —
        // duas requisições concorrentes criando o StockItem ao mesmo tempo
        // podem colidir na constraint única. Aceitável pro escopo deste
        // projeto; em produção valeria um upsert atômico (ON CONFLICT).
        StockItem stockItem = stockItemRepository.findForUpdate(productId, warehouseId)
                .orElseGet(() -> stockItemRepository.save(new StockItem(product, warehouse)));

        BigDecimal newQuantity = type == MovementType.ENTRADA
                ? stockItem.getQuantity().add(quantity)
                : stockItem.getQuantity().subtract(quantity);

        if (newQuantity.signum() < 0) {
            throw new BusinessException(
                    "Estoque insuficiente de " + product.getName() + " no almoxarifado " + warehouse.getName() + ".");
        }

        stockItem.setQuantity(newQuantity);

        StockMovement movement = new StockMovement(product, warehouse, type, quantity, referenceType, referenceId);
        // saveAndFlush (não save) pra garantir que o INSERT já rodou e o
        // @CreationTimestamp foi de fato preenchido antes de mapear a
        // resposta — com save() puro o flush só acontece no commit da
        // transação e createdAt volta null no DTO.
        return stockMovementRepository.saveAndFlush(movement);
    }
}
