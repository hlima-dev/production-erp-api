package com.hlima.erp.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.catalog.service.ProductService;
import com.hlima.erp.inventory.entity.MovementType;
import com.hlima.erp.inventory.entity.StockItem;
import com.hlima.erp.inventory.entity.StockMovement;
import com.hlima.erp.inventory.entity.Warehouse;
import com.hlima.erp.inventory.mapper.StockMapper;
import com.hlima.erp.inventory.repository.StockItemRepository;
import com.hlima.erp.inventory.repository.StockMovementRepository;
import com.hlima.erp.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * StockService.applyMovement é o único ponto de baixa/entrada de estoque —
 * usado pelo lançamento manual e (no módulo production) pela retirada de
 * insumos da ordem de produção. É o fluxo crítico citado no plano do
 * projeto: baixa de estoque tem que ser exata e nunca deixar saldo
 * negativo.
 */
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockItemRepository stockItemRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProductService productService;
    @Mock
    private WarehouseService warehouseService;
    @Mock
    private StockMapper mapper;

    private StockService stockService;

    private final UUID productId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();
    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        stockService = new StockService(stockItemRepository, stockMovementRepository, productService, warehouseService, mapper);

        product = mock(Product.class);
        warehouse = mock(Warehouse.class);
        when(productService.getOrThrow(productId)).thenReturn(product);
        when(warehouseService.getOrThrow(warehouseId)).thenReturn(warehouse);
        // lenient: o teste de saída maior que o saldo nunca chega a salvar o movimento.
        org.mockito.Mockito.lenient().when(stockMovementRepository.saveAndFlush(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void entradaAumentaOSaldoExistente() {
        StockItem existing = new StockItem(product, warehouse);
        existing.setQuantity(new BigDecimal("10.000000"));
        when(stockItemRepository.findForUpdate(productId, warehouseId)).thenReturn(Optional.of(existing));

        StockMovement movement = stockService.applyMovement(
                productId, warehouseId, MovementType.ENTRADA, new BigDecimal("5"), null, null);

        assertThat(existing.getQuantity()).isEqualByComparingTo("15");
        assertThat(movement.getType()).isEqualTo(MovementType.ENTRADA);
        assertThat(movement.getQuantity()).isEqualByComparingTo("5");
    }

    @Test
    void saidaDentroDoSaldoBaixaCorretamente() {
        StockItem existing = new StockItem(product, warehouse);
        existing.setQuantity(new BigDecimal("10"));
        when(stockItemRepository.findForUpdate(productId, warehouseId)).thenReturn(Optional.of(existing));

        stockService.applyMovement(productId, warehouseId, MovementType.SAIDA, new BigDecimal("4"), null, null);

        assertThat(existing.getQuantity()).isEqualByComparingTo("6");
    }

    @Test
    void saidaExatamenteIgualAoSaldoZeraSemErro() {
        StockItem existing = new StockItem(product, warehouse);
        existing.setQuantity(new BigDecimal("10"));
        when(stockItemRepository.findForUpdate(productId, warehouseId)).thenReturn(Optional.of(existing));

        stockService.applyMovement(productId, warehouseId, MovementType.SAIDA, new BigDecimal("10"), null, null);

        assertThat(existing.getQuantity()).isEqualByComparingTo("0");
    }

    @Test
    void saidaMaiorQueOSaldoLancaBusinessExceptionSemAlterarNada() {
        StockItem existing = new StockItem(product, warehouse);
        existing.setQuantity(new BigDecimal("5"));
        when(stockItemRepository.findForUpdate(productId, warehouseId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                stockService.applyMovement(productId, warehouseId, MovementType.SAIDA, new BigDecimal("10"), null, null))
                .isInstanceOf(BusinessException.class);

        assertThat(existing.getQuantity()).isEqualByComparingTo("5");
        verify(stockMovementRepository, never()).saveAndFlush(any());
    }

    @Test
    void primeiroMovimentoDeUmProdutoCriaOStockItemComSaldoZeroInicial() {
        when(stockItemRepository.findForUpdate(productId, warehouseId)).thenReturn(Optional.empty());
        when(stockItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        stockService.applyMovement(productId, warehouseId, MovementType.ENTRADA, new BigDecimal("3"), null, null);

        verify(stockItemRepository).save(any());
    }

    @Test
    void retiradaDeProducaoRegistraReferenciaDaOrdem() {
        StockItem existing = new StockItem(product, warehouse);
        existing.setQuantity(new BigDecimal("100"));
        when(stockItemRepository.findForUpdate(productId, warehouseId)).thenReturn(Optional.of(existing));
        UUID opId = UUID.randomUUID();

        StockMovement movement = stockService.applyMovement(
                productId, warehouseId, MovementType.RETIRADA_PRODUCAO, new BigDecimal("15"), "PRODUCTION_ORDER", opId);

        assertThat(movement.getReferenceType()).isEqualTo("PRODUCTION_ORDER");
        assertThat(movement.getReferenceId()).isEqualTo(opId);
        assertThat(existing.getQuantity()).isEqualByComparingTo("85");
    }
}
