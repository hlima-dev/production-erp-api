package com.hlima.erp.production.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.hlima.erp.production.entity.ProductionOrder;
import com.hlima.erp.production.entity.ProductionOrderStatus;
import com.hlima.erp.production.mapper.ProductionOrderMapper;
import com.hlima.erp.production.repository.ProductionOrderRepository;
import com.hlima.erp.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Fluxo mais crítico do projeto: iniciar uma ordem de produção precisa
 * calcular certo os insumos a partir da ficha técnica × quantidade
 * planejada, validar TODO o estoque antes de baixar qualquer coisa, e só
 * então retirar — sempre via StockService, nunca mexendo em estoque
 * diretamente.
 */
@ExtendWith(MockitoExtension.class)
class ProductionOrderServiceTest {

    @Mock
    private ProductionOrderRepository productionOrderRepository;
    @Mock
    private ProductService productService;
    @Mock
    private WarehouseService warehouseService;
    @Mock
    private BillOfMaterialService billOfMaterialService;
    @Mock
    private StockService stockService;
    @Mock
    private ProductionOrderMapper mapper;

    private ProductionOrderService productionOrderService;

    private final UUID orderId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();
    private final UUID finishedProductId = UUID.randomUUID();
    private final UUID ingredientId = UUID.randomUUID();

    private Product finishedProduct;
    private Warehouse warehouse;
    private Product ingredient;

    @BeforeEach
    void setUp() {
        productionOrderService = new ProductionOrderService(
                productionOrderRepository, productService, warehouseService, billOfMaterialService, stockService, mapper);

        finishedProduct = mock(Product.class);
        warehouse = mock(Warehouse.class);
        ingredient = mock(Product.class);

        // lenient: nem todo teste chega a usar todos esses getters (ex: o
        // teste de status inválido falha antes de olhar pra ficha técnica).
        org.mockito.Mockito.lenient().when(finishedProduct.getId()).thenReturn(finishedProductId);
        org.mockito.Mockito.lenient().when(finishedProduct.getName()).thenReturn("Pizza Individual Queijo");
        org.mockito.Mockito.lenient().when(warehouse.getId()).thenReturn(warehouseId);
        org.mockito.Mockito.lenient().when(ingredient.getId()).thenReturn(ingredientId);
        org.mockito.Mockito.lenient().when(ingredient.getName()).thenReturn("Queijo mussarela");
    }

    private ProductionOrder abertaOrder(BigDecimal plannedQuantity) {
        ProductionOrder order = new ProductionOrder(1L, finishedProduct, warehouse, plannedQuantity, null);
        // id só é preenchido pelo Hibernate na persistência real — como o
        // código sob teste usa order.getId() (referenceId da movimentação
        // de estoque), simulamos isso via reflexão pra bater com o id
        // usado no repositório mockado.
        org.springframework.test.util.ReflectionTestUtils.setField(order, "id", orderId);
        when(productionOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        return order;
    }

    private BillOfMaterial bomWith(BigDecimal quantityPerUnit) {
        BillOfMaterial bom = new BillOfMaterial(finishedProduct);
        bom.replaceItems(List.of(new BillOfMaterialItem(ingredient, quantityPerUnit)));
        return bom;
    }

    @Test
    void iniciarComEstoqueSuficienteRetiraQuantidadeCalculadaEAvancaStatus() {
        ProductionOrder order = abertaOrder(new BigDecimal("100"));
        when(billOfMaterialService.findEntityByProductId(finishedProductId))
                .thenReturn(Optional.of(bomWith(new BigDecimal("0.2"))));
        when(stockService.getAvailableQuantity(ingredientId, warehouseId)).thenReturn(new BigDecimal("50"));

        productionOrderService.start(orderId);

        // 100 pizzas x 0.2kg = 20kg retirados.
        verify(stockService).applyMovement(
                eq(ingredientId), eq(warehouseId), eq(MovementType.RETIRADA_PRODUCAO),
                eq(new BigDecimal("20.0")), eq("PRODUCTION_ORDER"), eq(orderId));
        assertThat(order.getStatus()).isEqualTo(ProductionOrderStatus.EM_PRODUCAO);
        assertThat(order.getStartedAt()).isNotNull();
    }

    @Test
    void iniciarComEstoqueInsuficienteLancaExcecaoSemRetirarNada() {
        abertaOrder(new BigDecimal("1000"));
        when(billOfMaterialService.findEntityByProductId(finishedProductId))
                .thenReturn(Optional.of(bomWith(new BigDecimal("0.2"))));
        // Precisa de 200kg, só tem 50.
        when(stockService.getAvailableQuantity(ingredientId, warehouseId)).thenReturn(new BigDecimal("50"));

        assertThatThrownBy(() -> productionOrderService.start(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Queijo mussarela");

        verify(stockService, never()).applyMovement(any(), any(), any(), any(), any(), any());
    }

    @Test
    void iniciarProdutoSemFichaTecnicaLancaExcecao() {
        abertaOrder(new BigDecimal("10"));
        when(billOfMaterialService.findEntityByProductId(finishedProductId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productionOrderService.start(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ficha técnica");
    }

    @Test
    void iniciarOrdemQueNaoEstaAbertaLancaExcecao() {
        ProductionOrder order = abertaOrder(new BigDecimal("10"));
        order.setStatus(ProductionOrderStatus.CONCLUIDA);

        assertThatThrownBy(() -> productionOrderService.start(orderId))
                .isInstanceOf(BusinessException.class);
        verify(billOfMaterialService, never()).findEntityByProductId(any());
    }

    @Test
    void concluirSemQuantidadeInformadaUsaAPlanejada() {
        ProductionOrder order = abertaOrder(new BigDecimal("100"));
        order.setStatus(ProductionOrderStatus.EM_PRODUCAO);

        productionOrderService.complete(orderId, null);

        verify(stockService).applyMovement(
                eq(finishedProductId), eq(warehouseId), eq(MovementType.ENTRADA),
                eq(new BigDecimal("100")), eq("PRODUCTION_ORDER"), eq(orderId));
        assertThat(order.getProducedQuantity()).isEqualByComparingTo("100");
        assertThat(order.getStatus()).isEqualTo(ProductionOrderStatus.CONCLUIDA);
    }

    @Test
    void concluirComQuantidadeDiferenteUsaAInformada() {
        ProductionOrder order = abertaOrder(new BigDecimal("100"));
        order.setStatus(ProductionOrderStatus.EM_PRODUCAO);

        productionOrderService.complete(orderId, new CompleteProductionRequest(new BigDecimal("97")));

        verify(stockService).applyMovement(
                eq(finishedProductId), eq(warehouseId), eq(MovementType.ENTRADA),
                eq(new BigDecimal("97")), eq("PRODUCTION_ORDER"), eq(orderId));
        assertThat(order.getProducedQuantity()).isEqualByComparingTo("97");
    }

    @Test
    void cancelarOrdemJaEmProducaoLancaExcecao() {
        ProductionOrder order = abertaOrder(new BigDecimal("10"));
        order.setStatus(ProductionOrderStatus.EM_PRODUCAO);

        assertThatThrownBy(() -> productionOrderService.cancel(orderId))
                .isInstanceOf(BusinessException.class);
    }
}
