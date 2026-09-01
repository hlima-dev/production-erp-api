package com.hlima.erp.fiscal.service;

import com.hlima.erp.fiscal.dto.InvoiceRequest;
import com.hlima.erp.fiscal.dto.InvoiceResponse;
import com.hlima.erp.fiscal.entity.Invoice;
import com.hlima.erp.fiscal.entity.InvoiceItem;
import com.hlima.erp.fiscal.entity.InvoiceStatus;
import com.hlima.erp.fiscal.mapper.InvoiceMapper;
import com.hlima.erp.fiscal.repository.InvoiceRepository;
import com.hlima.erp.sales.entity.Order;
import com.hlima.erp.sales.entity.OrderItem;
import com.hlima.erp.sales.entity.OrderStatus;
import com.hlima.erp.sales.service.OrderService;
import com.hlima.erp.shared.exception.BusinessException;
import com.hlima.erp.shared.exception.ConflictException;
import com.hlima.erp.shared.exception.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emissão de NF-e SIMULADA a partir de um pedido faturável (status
 * EM_SEPARACAO). Não transmite nada à SEFAZ — gera só a estrutura do
 * documento (chave de acesso em formato de 44 dígitos, não validável, e um
 * cálculo simplificado de ICMS/PIS/COFINS com alíquotas fixas). Ao emitir,
 * o pedido é marcado como FATURADO via {@link OrderService#markInvoiced}.
 */
@Service
@Transactional(readOnly = true)
public class InvoiceService {

    // Alíquotas fixas (simplificação — numa NF-e real variam por NCM, CFOP,
    // regime tributário do emissor e UF de origem/destino).
    private static final BigDecimal ICMS_RATE = new BigDecimal("18.00");
    private static final BigDecimal PIS_RATE = new BigDecimal("1.65");
    private static final BigDecimal COFINS_RATE = new BigDecimal("7.60");
    private static final String CFOP_VENDA_ESTADUAL = "5102";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final InvoiceRepository invoiceRepository;
    private final OrderService orderService;
    private final InvoiceMapper mapper;

    public InvoiceService(InvoiceRepository invoiceRepository, OrderService orderService, InvoiceMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.orderService = orderService;
        this.mapper = mapper;
    }

    public List<InvoiceResponse> list(InvoiceStatus status) {
        List<Invoice> invoices = status == null ? invoiceRepository.findAllBy() : invoiceRepository.findByStatus(status);
        return invoices.stream().map(mapper::toResponse).toList();
    }

    public InvoiceResponse findById(UUID id) {
        return mapper.toResponse(getWithItemsOrThrow(id));
    }

    public Invoice getWithItemsOrThrow(UUID id) {
        return invoiceRepository.findWithItemsById(id)
                .orElseThrow(() -> new NotFoundException("Nota fiscal"));
    }

    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        Order order = orderService.getWithItemsOrThrow(request.orderId());

        if (order.getStatus() != OrderStatus.EM_SEPARACAO) {
            throw new BusinessException(
                    "Só é possível faturar um pedido em separação (status atual: " + order.getStatus() + ").");
        }
        if (invoiceRepository.existsByOrderId(order.getId())) {
            throw new ConflictException("Já existe uma nota fiscal emitida pra este pedido.");
        }

        Long invoiceNumber = invoiceRepository.nextInvoiceNumber();
        Invoice invoice = new Invoice(order, invoiceNumber, generateAccessKey());
        invoice.replaceItems(buildItems(order.getItems()));

        // saveAndFlush: mesmo motivo dos outros módulos — garante que o
        // INSERT já rodou antes de mapear a resposta (createdAt/issueDate).
        Invoice saved = invoiceRepository.saveAndFlush(invoice);

        orderService.markInvoiced(order.getId());

        return mapper.toResponse(saved);
    }

    /**
     * Cancelamento simulado: só troca o status pra CANCELADA. Uma NF-e real
     * cancelada exige um evento de cancelamento transmitido à SEFEAZ dentro
     * de uma janela de tempo, e normalmente reverteria o faturamento do
     * pedido — fora do escopo deste projeto de portfólio.
     */
    @Transactional
    public InvoiceResponse cancel(UUID id) {
        Invoice invoice = getWithItemsOrThrow(id);
        if (invoice.getStatus() != InvoiceStatus.EMITIDA) {
            throw new BusinessException("Nota fiscal já está cancelada.");
        }
        invoice.setStatus(InvoiceStatus.CANCELADA);
        return mapper.toResponse(invoice);
    }

    private List<InvoiceItem> buildItems(List<OrderItem> orderItems) {
        return orderItems.stream().map(this::toInvoiceItem).toList();
    }

    private InvoiceItem toInvoiceItem(OrderItem orderItem) {
        BigDecimal subtotal = orderItem.getSubtotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal icmsAmount = percentOf(subtotal, ICMS_RATE);
        BigDecimal pisAmount = percentOf(subtotal, PIS_RATE);
        BigDecimal cofinsAmount = percentOf(subtotal, COFINS_RATE);

        return new InvoiceItem(
                orderItem.getProduct(), orderItem.getQuantity(), orderItem.getUnitPrice(), subtotal, CFOP_VENDA_ESTADUAL,
                ICMS_RATE, icmsAmount, PIS_RATE, pisAmount, COFINS_RATE, cofinsAmount);
    }

    private BigDecimal percentOf(BigDecimal amount, BigDecimal ratePercent) {
        return amount.multiply(ratePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Chave de acesso simulada: 44 dígitos numéricos, gerados localmente.
     * Só o FORMATO (44 dígitos) segue o padrão da NF-e real — não é uma
     * chave calculada com os campos oficiais (UF, CNPJ, dígito verificador
     * módulo 11 etc.) nem válida perante a SEFAZ.
     */
    private String generateAccessKey() {
        StringBuilder sb = new StringBuilder(44);
        for (int i = 0; i < 44; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
