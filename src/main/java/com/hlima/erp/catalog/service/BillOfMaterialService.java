package com.hlima.erp.catalog.service;

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
import com.hlima.erp.shared.exception.NotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BillOfMaterialService {

    private final BillOfMaterialRepository billOfMaterialRepository;
    private final ProductService productService;
    private final BillOfMaterialMapper mapper;

    public BillOfMaterialService(
            BillOfMaterialRepository billOfMaterialRepository,
            ProductService productService,
            BillOfMaterialMapper mapper
    ) {
        this.billOfMaterialRepository = billOfMaterialRepository;
        this.productService = productService;
        this.mapper = mapper;
    }

    public BillOfMaterialResponse getByProductId(UUID productId) {
        return mapper.toResponse(getEntityByProductId(productId));
    }

    BillOfMaterial getEntityByProductId(UUID productId) {
        return billOfMaterialRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Ficha técnica"));
    }

    // Usado pelo módulo production pra checar se o produto tem ficha
    // técnica cadastrada antes de iniciar uma ordem de produção — sem
    // lançar NotFoundException, já que "não ter ficha técnica" ali é uma
    // regra de negócio (BusinessException), não um recurso não encontrado.
    public Optional<BillOfMaterial> findEntityByProductId(UUID productId) {
        return billOfMaterialRepository.findByProductId(productId);
    }

    @Transactional
    public BillOfMaterialResponse save(UUID productId, BillOfMaterialRequest request) {
        Product product = productService.getOrThrow(productId);
        if (product.getType() == ProductType.MATERIA_PRIMA) {
            throw new BusinessException(
                    "Só produtos semi-acabados ou acabados podem ter ficha técnica ("
                            + product.getName() + " é matéria-prima).");
        }

        List<UUID> ingredientIds = request.items().stream().map(BillOfMaterialItemRequest::ingredientId).toList();
        if (new HashSet<>(ingredientIds).size() != ingredientIds.size()) {
            throw new BusinessException("Insumo duplicado na ficha técnica.");
        }
        for (UUID ingredientId : ingredientIds) {
            validateIngredient(product, ingredientId);
        }

        BillOfMaterial bom = billOfMaterialRepository.findByProductId(productId)
                .orElseGet(() -> new BillOfMaterial(product));

        bom.setNotes(request.notes());

        List<BillOfMaterialItem> items = request.items().stream()
                .map(this::toItem)
                .toList();
        bom.replaceItems(items);

        return mapper.toResponse(billOfMaterialRepository.save(bom));
    }

    // Um insumo precisa ser matéria-prima ou semi-acabado (produto acabado
    // não entra como insumo de mais nada), não pode ser o próprio produto, e
    // — com BOM em múltiplos níveis — não pode já depender (direta ou
    // indiretamente) do produto que está recebendo essa ficha técnica, senão
    // formaria um ciclo (A precisa de B que precisa de A).
    private void validateIngredient(Product product, UUID ingredientId) {
        if (product.getId().equals(ingredientId)) {
            throw new BusinessException("Um produto não pode ser insumo dele mesmo.");
        }

        Product ingredient = productService.getOrThrow(ingredientId);
        if (ingredient.getType() == ProductType.PRODUTO_ACABADO) {
            throw new BusinessException(
                    "Produto acabado (" + ingredient.getName() + ") não pode ser usado como insumo — só matéria-prima ou semi-acabado.");
        }

        if (dependsOn(ingredientId, product.getId(), new HashSet<>())) {
            throw new BusinessException(
                    "Dependência circular: " + ingredient.getName() + " já depende (direta ou indiretamente) de "
                            + product.getName() + ".");
        }
    }

    // Verdadeiro se a ficha técnica de `productId`, seguida recursivamente
    // pelas fichas técnicas dos insumos semi-acabados dela, chega em `target`.
    private boolean dependsOn(UUID productId, UUID target, Set<UUID> visited) {
        if (!visited.add(productId)) return false;

        return billOfMaterialRepository.findByProductId(productId)
                .map(BillOfMaterial::getItems)
                .orElse(List.of())
                .stream()
                .anyMatch(item -> {
                    UUID ingredientId = item.getIngredient().getId();
                    return ingredientId.equals(target) || dependsOn(ingredientId, target, visited);
                });
    }

    private BillOfMaterialItem toItem(BillOfMaterialItemRequest request) {
        Product ingredient = productService.getOrThrow(request.ingredientId());
        return new BillOfMaterialItem(ingredient, request.quantityPerUnit());
    }
}
