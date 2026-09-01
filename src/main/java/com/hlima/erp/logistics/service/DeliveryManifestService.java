package com.hlima.erp.logistics.service;

import com.hlima.erp.logistics.dto.DeliveryManifestRequest;
import com.hlima.erp.logistics.dto.DeliveryManifestResponse;
import com.hlima.erp.logistics.entity.DeliveryManifest;
import com.hlima.erp.logistics.entity.DeliveryManifestStatus;
import com.hlima.erp.logistics.entity.Driver;
import com.hlima.erp.logistics.entity.Vehicle;
import com.hlima.erp.logistics.mapper.DeliveryManifestMapper;
import com.hlima.erp.logistics.repository.DeliveryManifestRepository;
import com.hlima.erp.sales.entity.Order;
import com.hlima.erp.sales.entity.OrderStatus;
import com.hlima.erp.sales.service.OrderService;
import com.hlima.erp.shared.exception.BusinessException;
import com.hlima.erp.shared.exception.NotFoundException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Romaneio de expedição. Um pedido só entra num romaneio se estiver
 * FATURADO — ao ser incluído, é imediatamente marcado como EXPEDIDO (ver
 * comentário em {@link OrderStatus}); o status do romaneio em si
 * (PLANEJADO → EM_ROTA → CONCLUIDO) acompanha a rota do veículo, não o
 * status do pedido. Ao concluir o romaneio, todos os pedidos são marcados
 * como ENTREGUE.
 */
@Service
@Transactional(readOnly = true)
public class DeliveryManifestService {

    private final DeliveryManifestRepository deliveryManifestRepository;
    private final VehicleService vehicleService;
    private final DriverService driverService;
    private final OrderService orderService;
    private final DeliveryManifestMapper mapper;

    public DeliveryManifestService(
            DeliveryManifestRepository deliveryManifestRepository,
            VehicleService vehicleService,
            DriverService driverService,
            OrderService orderService,
            DeliveryManifestMapper mapper
    ) {
        this.deliveryManifestRepository = deliveryManifestRepository;
        this.vehicleService = vehicleService;
        this.driverService = driverService;
        this.orderService = orderService;
        this.mapper = mapper;
    }

    public List<DeliveryManifestResponse> list(DeliveryManifestStatus status) {
        List<DeliveryManifest> manifests = status == null
                ? deliveryManifestRepository.findAllBy()
                : deliveryManifestRepository.findByStatus(status);
        return manifests.stream().map(mapper::toResponse).toList();
    }

    public DeliveryManifestResponse findById(UUID id) {
        return mapper.toResponse(getWithOrdersOrThrow(id));
    }

    public DeliveryManifest getWithOrdersOrThrow(UUID id) {
        return deliveryManifestRepository.findWithOrdersById(id)
                .orElseThrow(() -> new NotFoundException("Romaneio"));
    }

    @Transactional
    public DeliveryManifestResponse create(DeliveryManifestRequest request) {
        Vehicle vehicle = vehicleService.getOrThrow(request.vehicleId());
        Driver driver = driverService.getOrThrow(request.driverId());

        Set<Order> orders = new HashSet<>();
        for (UUID orderId : request.orderIds()) {
            Order order = orderService.getOrThrow(orderId);
            if (order.getStatus() != OrderStatus.FATURADO) {
                throw new BusinessException(
                        "Pedido #" + order.getOrderNumber() + " não pode entrar no romaneio: status atual é "
                                + order.getStatus() + ", esperado FATURADO.");
            }
            if (deliveryManifestRepository.existsByOrders_Id(orderId)) {
                throw new BusinessException("Pedido #" + order.getOrderNumber() + " já está em outro romaneio.");
            }
            orders.add(order);
        }

        DeliveryManifest manifest = new DeliveryManifest(
                deliveryManifestRepository.nextManifestNumber(), vehicle, driver, request.notes());
        manifest.setOrders(orders);

        DeliveryManifest saved = deliveryManifestRepository.saveAndFlush(manifest);

        // Incluir no romaneio já expede o pedido — ver Javadoc da classe.
        orders.forEach(order -> orderService.markShipped(order.getId()));

        return mapper.toResponse(saved);
    }

    @Transactional
    public DeliveryManifestResponse start(UUID id) {
        DeliveryManifest manifest = getWithOrdersOrThrow(id);
        requireStatus(manifest, "iniciar a rota de", DeliveryManifestStatus.PLANEJADO);
        manifest.setStatus(DeliveryManifestStatus.EM_ROTA);
        manifest.setDepartedAt(Instant.now());
        return mapper.toResponse(manifest);
    }

    @Transactional
    public DeliveryManifestResponse complete(UUID id) {
        DeliveryManifest manifest = getWithOrdersOrThrow(id);
        requireStatus(manifest, "concluir", DeliveryManifestStatus.EM_ROTA);

        manifest.getOrders().forEach(order -> orderService.markDelivered(order.getId()));

        manifest.setStatus(DeliveryManifestStatus.CONCLUIDO);
        manifest.setCompletedAt(Instant.now());
        return mapper.toResponse(manifest);
    }

    private void requireStatus(DeliveryManifest manifest, String action, DeliveryManifestStatus expected) {
        if (manifest.getStatus() != expected) {
            throw new BusinessException(
                    "Não é possível " + action + " o romaneio: status atual é " + manifest.getStatus()
                            + ", esperado " + expected + ".");
        }
    }
}
