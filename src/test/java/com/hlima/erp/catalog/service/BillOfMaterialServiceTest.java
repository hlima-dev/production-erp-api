package com.hlima.erp.catalog.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hlima.erp.catalog.dto.BillOfMaterialItemRequest;
import com.hlima.erp.catalog.dto.BillOfMaterialRequest;
import com.hlima.erp.catalog.dto.BillOfMaterialResponse;
import com.hlima.erp.catalog.entity.BillOfMaterial;
import com.hlima.erp.catalog.entity.BillOfMaterialItem;
import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.catalog.entity.ProductType;
import com.hlima.erp.catalog.mapper.BillOfMaterialMapper;
import com.hlima.erp.catalog.repository.BillOfMaterialRepository;
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
 * BOM em múltiplos níveis (matéria-prima → semi-acabado → produto acabado):
 * cobre as validações que impedem uma ficha técnica malformada — insumo
 * inválido (acabado, ele mesmo) ou uma dependência circular entre semi-
 * acabados, que travaria em loop infinito na hora de calcular os insumos de
 * uma ordem de produção.
 */
@ExtendWith(MockitoExtension.class)
class BillOfMaterialServiceTest {

    @Mock
    private BillOfMaterialRepository billOfMaterialRepository;
    @Mock
    private ProductService productService;
    @Mock
    private BillOfMaterialMapper mapper;

    private BillOfMaterialService service;

    private final UUID finishedId = UUID.randomUUID();
    private final UUID semiId = UUID.randomUUID();
    private final UUID rawId = UUID.randomUUID();

    private Product finished;
    private Product semi;
    private Product raw;

    @BeforeEach
    void setUp() {
        service = new BillOfMaterialService(billOfMaterialRepository, productService, mapper);

        finished = mock(Product.class);
        semi = mock(Product.class);
        raw = mock(Product.class);

        lenient().when(finished.getId()).thenReturn(finishedId);
        lenient().when(finished.getName()).thenReturn("Mini pizza de queijo");
        lenient().when(finished.getType()).thenReturn(ProductType.PRODUTO_ACABADO);

        lenient().when(semi.getId()).thenReturn(semiId);
        lenient().when(semi.getName()).thenReturn("Massa de pizza");
        lenient().when(semi.getType()).thenReturn(ProductType.SEMI_ACABADO);

        lenient().when(raw.getId()).thenReturn(rawId);
        lenient().when(raw.getName()).thenReturn("Farinha de trigo");
        lenient().when(raw.getType()).thenReturn(ProductType.MATERIA_PRIMA);
    }

    private BillOfMaterialRequest requestWith(UUID... ingredientIds) {
        List<BillOfMaterialItemRequest> items = List.of(ingredientIds).stream()
                .map(id -> new BillOfMaterialItemRequest(id, BigDecimal.ONE))
                .toList();
        return new BillOfMaterialRequest(null, items);
    }

    @Test
    void materiaPrimaNaoPodeTerFichaTecnica() {
        when(productService.getOrThrow(rawId)).thenReturn(raw);

        assertThatThrownBy(() -> service.save(rawId, requestWith(rawId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("matéria-prima");
    }

    @Test
    void insumoNaoPodeSerOProprioProduto() {
        when(productService.getOrThrow(finishedId)).thenReturn(finished);

        assertThatThrownBy(() -> service.save(finishedId, requestWith(finishedId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("dele mesmo");
    }

    @Test
    void produtoAcabadoNaoPodeSerUsadoComoInsumo() {
        Product outroAcabado = mock(Product.class);
        UUID outroAcabadoId = UUID.randomUUID();
        when(outroAcabado.getName()).thenReturn("Pizza grande");
        when(outroAcabado.getType()).thenReturn(ProductType.PRODUTO_ACABADO);

        when(productService.getOrThrow(finishedId)).thenReturn(finished);
        when(productService.getOrThrow(outroAcabadoId)).thenReturn(outroAcabado);

        assertThatThrownBy(() -> service.save(finishedId, requestWith(outroAcabadoId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Produto acabado");
    }

    @Test
    void insumoDuplicadoNaFichaTecnicaEhRejeitado() {
        // Duplicidade é checada antes de resolver cada insumo individualmente
        // — não precisa mockar productService.getOrThrow do insumo aqui.
        when(productService.getOrThrow(finishedId)).thenReturn(finished);

        assertThatThrownBy(() -> service.save(finishedId, requestWith(semiId, semiId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("duplicado");
    }

    @Test
    void dependenciaCircularEntreSemiAcabadosEhRejeitada() {
        // Massa de pizza (semi) já tem ficha técnica usando Mini pizza
        // (acabado) como insumo seria inválido por outro motivo — aqui o
        // cenário é dois semi-acabados: "semi" já depende de "outroSemi";
        // tentar salvar a ficha técnica de "outroSemi" usando "semi" como
        // insumo formaria um ciclo (outroSemi -> semi -> outroSemi).
        Product outroSemi = mock(Product.class);
        UUID outroSemiId = UUID.randomUUID();
        when(outroSemi.getId()).thenReturn(outroSemiId);
        when(outroSemi.getName()).thenReturn("Molho de tomate");
        when(outroSemi.getType()).thenReturn(ProductType.SEMI_ACABADO);

        BillOfMaterial semiBom = new BillOfMaterial(semi);
        semiBom.replaceItems(List.of(new BillOfMaterialItem(outroSemi, BigDecimal.ONE)));
        when(billOfMaterialRepository.findByProductId(semiId)).thenReturn(Optional.of(semiBom));

        when(productService.getOrThrow(outroSemiId)).thenReturn(outroSemi);
        when(productService.getOrThrow(semiId)).thenReturn(semi);

        assertThatThrownBy(() -> service.save(outroSemiId, requestWith(semiId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("circular");
    }

    @Test
    void salvaFichaTecnicaValidaDeProdutoAcabadoComInsumoSemiAcabado() {
        when(productService.getOrThrow(finishedId)).thenReturn(finished);
        when(productService.getOrThrow(semiId)).thenReturn(semi);
        when(billOfMaterialRepository.findByProductId(semiId)).thenReturn(Optional.empty());
        when(billOfMaterialRepository.findByProductId(finishedId)).thenReturn(Optional.empty());
        when(billOfMaterialRepository.save(any(BillOfMaterial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(BillOfMaterial.class)))
                .thenReturn(new BillOfMaterialResponse(finishedId, finishedId, "Mini pizza de queijo", null, List.of()));

        service.save(finishedId, requestWith(semiId));

        verify(billOfMaterialRepository).save(any(BillOfMaterial.class));
    }
}
