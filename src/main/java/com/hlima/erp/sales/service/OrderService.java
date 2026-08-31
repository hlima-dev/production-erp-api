package com.hlima.erp.sales.service;

import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.catalog.service.ProductService;
import com.hlima.erp.sales.dto.OrderRequest;
import com.hlima.erp.sales.dto.OrderResponse;
import com.hlima.erp.sales.entity.Customer;
import com.hlima.erp.sales.entity.Order;
import com.hlima.erp.sales.entity.OrderItem;
import com.hlima.erp.sales.entity.OrderStatus;
import com.hlima.erp.sales.mapper.OrderMapper;
import com.hlima.erp.sales.repository.OrderRepository;
import com.hlima.erp.shared.exception.BusinessException;
import com.hlima.erp.shared.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Único lugar que altera o status de um pedido. Os módulos fiscal
 * (emissão de NF-e) e logistics (romaneio de expedição) chamam
 * {@link #markInvoiced(UUID)} e {@link #markShipped(UUID)} respectivamente
 * em vez de expor um endpoint genérico de "trocar status" — cada transição
 * só pode ser disparada por quem realmente sabe que a etapa aconteceu.
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderMapper mapper;

    public OrderService(
            OrderRepository orderRepository,
            CustomerService customerService,
            ProductService productService,
            OrderMapper mapper
    ) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.mapper = mapper;
    }

    public List<OrderResponse> list(OrderStatus status) {
        List<Order> orders = status == null ? orderRepository.findAllBy() : orderRepository.findByStatus(status);
        return orders.stream().map(mapper::toResponse).toList();
    }

    public OrderResponse findById(UUID id) {
        return mapper.toResponse(getWithItemsOrThrow(id));
    }

    public Order getOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido"));
    }

    public Order getWithItemsOrThrow(UUID id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new NotFoundException("Pedido"));
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Customer customer = customerService.getOrThrow(request.customerId());

        Order order = new Order(customer, orderRepository.nextOrderNumber());
        order.replaceItems(buildItems(request));

        // saveAndFlush pelo mesmo motivo do StockService: garantir que o
        // INSERT já rodou antes de mapear a resposta, senão createdAt vem
        // null no DTO (só é preenchido no flush, que com save() puro só
        // acontece no commit da transação).
        return mapper.toResponse(orderRepository.saveAndFlush(order));
    }

    @Transactional
    public OrderResponse update(UUID id, OrderRequest request) {
        Order order = getWithItemsOrThrow(id);
        requireStatus(order, "editar", OrderStatus.RASCUNHO);

        Customer customer = customerService.getOrThrow(request.customerId());
        order.setCustomer(customer);
        order.replaceItems(buildItems(request));

        return mapper.toResponse(order);
    }

    @Transactional
    public OrderResponse confirm(UUID id) {
        Order order = getOrThrow(id);
        requireStatus(order, "confirmar", OrderStatus.RASCUNHO);
        order.setStatus(OrderStatus.CONFIRMADO);
        return mapper.toResponse(order);
    }

    @Transactional
    public OrderResponse startSeparation(UUID id) {
        Order order = getOrThrow(id);
        requireStatus(order, "iniciar separação de", OrderStatus.CONFIRMADO);
        order.setStatus(OrderStatus.EM_SEPARACAO);
        return mapper.toResponse(order);
    }

    /** Chamado pelo módulo fiscal ao emitir a NF-e do pedido. */
    @Transactional
    public void markInvoiced(UUID id) {
        Order order = getOrThrow(id);
        requireStatus(order, "faturar", OrderStatus.EM_SEPARACAO);
        order.setStatus(OrderStatus.FATURADO);
    }

    /** Chamado pelo módulo logistics ao incluir o pedido num romaneio. */
    @Transactional
    public void markShipped(UUID id) {
        Order order = getOrThrow(id);
        requireStatus(order, "expedir", OrderStatus.FATURADO);
        order.setStatus(OrderStatus.EXPEDIDO);
    }

    @Transactional
    public OrderResponse markDelivered(UUID id) {
        Order order = getOrThrow(id);
        requireStatus(order, "entregar", OrderStatus.EXPEDIDO);
        order.setStatus(OrderStatus.ENTREGUE);
        return mapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(UUID id) {
        Order order = getOrThrow(id);
        if (order.getStatus() != OrderStatus.RASCUNHO
                && order.getStatus() != OrderStatus.CONFIRMADO
                && order.getStatus() != OrderStatus.EM_SEPARACAO) {
            throw new BusinessException(
                    "Pedido não pode ser cancelado no status " + order.getStatus() + " (já faturado).");
        }
        order.setStatus(OrderStatus.CANCELADO);
        return mapper.toResponse(order);
    }

    private List<OrderItem> buildItems(OrderRequest request) {
        return request.items().stream()
                .map(itemRequest -> {
                    Product product = productService.getOrThrow(itemRequest.productId());
                    BigDecimal unitPrice = itemRequest.unitPrice() != null ? itemRequest.unitPrice() : product.getPrice();
                    if (unitPrice == null) {
                        throw new BusinessException(
                                "Produto " + product.getName() + " não tem preço cadastrado — informe o preço no item.");
                    }
                    return new OrderItem(product, itemRequest.quantity(), unitPrice);
                })
                .toList();
    }

    private void requireStatus(Order order, String action, OrderStatus expected) {
        if (order.getStatus() != expected) {
            throw new BusinessException(
                    "Não é possível " + action + " o pedido: status atual é " + order.getStatus()
                            + ", esperado " + expected + ".");
        }
    }
}
