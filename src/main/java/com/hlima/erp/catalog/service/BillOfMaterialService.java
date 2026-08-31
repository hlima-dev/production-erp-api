package com.hlima.erp.catalog.service;

import com.hlima.erp.catalog.dto.BillOfMaterialItemRequest;
import com.hlima.erp.catalog.dto.BillOfMaterialRequest;
import com.hlima.erp.catalog.dto.BillOfMaterialResponse;
import com.hlima.erp.catalog.entity.BillOfMaterial;
import com.hlima.erp.catalog.entity.BillOfMaterialItem;
import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.catalog.mapper.BillOfMaterialMapper;
import com.hlima.erp.catalog.repository.BillOfMaterialRepository;
import com.hlima.erp.shared.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
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

        BillOfMaterial bom = billOfMaterialRepository.findByProductId(productId)
                .orElseGet(() -> new BillOfMaterial(product));

        bom.setNotes(request.notes());

        List<BillOfMaterialItem> items = request.items().stream()
                .map(this::toItem)
                .toList();
        bom.replaceItems(items);

        return mapper.toResponse(billOfMaterialRepository.save(bom));
    }

    private BillOfMaterialItem toItem(BillOfMaterialItemRequest request) {
        Product ingredient = productService.getOrThrow(request.ingredientId());
        return new BillOfMaterialItem(ingredient, request.quantityPerUnit());
    }
}
