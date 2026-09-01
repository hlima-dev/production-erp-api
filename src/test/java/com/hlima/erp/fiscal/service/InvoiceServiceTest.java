package com.hlima.erp.fiscal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.fiscal.dto.InvoiceRequest;
import com.hlima.erp.fiscal.entity.Invoice;
import com.hlima.erp.fiscal.entity.InvoiceStatus;
import com.hlima.erp.fiscal.mapper.InvoiceMapper;
import com.hlima.erp.fiscal.repository.InvoiceRepository;
import com.hlima.erp.sales.entity.Customer;
import com.hlima.erp.sales.entity.Order;
import com.hlima.erp.sales.entity.OrderItem;
import com.hlima.erp.sales.entity.OrderStatus;
import com.hlima.erp.sales.service.OrderService;
import com.hlima.erp.shared.exception.BusinessException;
import com.hlima.erp.shared.exception.ConflictException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cálculo de impostos simplificado (ICMS/PIS/COFINS) é o outro fluxo
 * crítico citado no plano do projeto — precisa bater exatamente com as
 * alíquotas fixas documentadas em InvoiceService, e a chave de acesso
 * simulada precisa sempre ter 44 dígitos.
 */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private InvoiceMapper mapper;

    private InvoiceService invoiceService;

    private final UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(invoiceRepository, orderService, mapper);
    }

    private Order emSeparacaoOrderWithOneItem(BigDecimal quantity, BigDecimal unitPrice) {
        Customer customer = mock(Customer.class);
        Order order = new Order(customer, 1000L);
        // id só é preenchido pelo Hibernate na persistência real — o
        // serviço usa order.getId() ao chamar markInvoiced, então
        // simulamos isso via reflexão pra bater com orderId.
        org.springframework.test.util.ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.EM_SEPARACAO);
        Product product = mock(Product.class);
        order.replaceItems(List.of(new OrderItem(product, quantity, unitPrice)));
        when(orderService.getWithItemsOrThrow(orderId)).thenReturn(order);
        return order;
    }

    @Test
    void faturarPedidoForaDeEmSeparacaoLancaExcecaoSemCriarNota() {
        Order order = emSeparacaoOrderWithOneItem(new BigDecimal("2"), new BigDecimal("22.00"));
        order.setStatus(OrderStatus.CONFIRMADO);

        assertThatThrownBy(() -> invoiceService.create(new InvoiceRequest(orderId)))
                .isInstanceOf(BusinessException.class);

        verify(invoiceRepository, never()).saveAndFlush(any());
        verify(orderService, never()).markInvoiced(any());
    }

    @Test
    void faturarPedidoJaComNotaLancaConflictException() {
        emSeparacaoOrderWithOneItem(new BigDecimal("2"), new BigDecimal("22.00"));
        when(invoiceRepository.existsByOrderId(any())).thenReturn(true);

        assertThatThrownBy(() -> invoiceService.create(new InvoiceRequest(orderId)))
                .isInstanceOf(ConflictException.class);

        verify(orderService, never()).markInvoiced(any());
    }

    @Test
    void faturarCalculaImpostosCorretamenteEMarcaPedidoComoFaturado() {
        // subtotal = 2 x 22.00 = 44.00
        emSeparacaoOrderWithOneItem(new BigDecimal("2"), new BigDecimal("22.00"));
        when(invoiceRepository.existsByOrderId(any())).thenReturn(false);
        when(invoiceRepository.nextInvoiceNumber()).thenReturn(1L);
        when(invoiceRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        invoiceService.create(new InvoiceRequest(orderId));

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).saveAndFlush(captor.capture());
        Invoice invoice = captor.getValue();

        assertThat(invoice.getTotalAmount()).isEqualByComparingTo("44.00");
        assertThat(invoice.getTotalIcms()).isEqualByComparingTo("7.92");   // 18% de 44
        assertThat(invoice.getTotalPis()).isEqualByComparingTo("0.73");   // 1.65% de 44 = 0.726 -> 0.73
        assertThat(invoice.getTotalCofins()).isEqualByComparingTo("3.34"); // 7.6% de 44 = 3.344 -> 3.34
        assertThat(invoice.getAccessKey()).hasSize(44);
        assertThat(invoice.getAccessKey()).matches("\\d{44}");
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.EMITIDA);

        verify(orderService).markInvoiced(orderId);
    }

    @Test
    void cancelarNotaJaCanceladaLancaExcecao() {
        Invoice invoice = new Invoice(mock(Order.class), 1L, "0".repeat(44));
        invoice.setStatus(InvoiceStatus.CANCELADA);
        when(invoiceRepository.findWithItemsById(any())).thenReturn(java.util.Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.cancel(UUID.randomUUID()))
                .isInstanceOf(BusinessException.class);
    }
}
