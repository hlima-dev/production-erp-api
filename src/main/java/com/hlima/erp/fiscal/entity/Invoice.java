package com.hlima.erp.fiscal.entity;

import com.hlima.erp.sales.entity.Order;
import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NF-e SIMULADA — não é transmitida à SEFAZ, não usa certificado digital e
 * a chave de acesso é gerada localmente (só o formato de 44 dígitos é
 * respeitado, não é uma chave válida). Serve pra demonstrar o fluxo de
 * faturamento do pedido, não substitui um emissor fiscal real.
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private Long invoiceNumber;

    @Column(name = "access_key", nullable = false, unique = true, length = 44)
    private String accessKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.EMITIDA;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_icms", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalIcms;

    @Column(name = "total_pis", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPis;

    @Column(name = "total_cofins", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCofins;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice(Order order, Long invoiceNumber, String accessKey) {
        this.order = order;
        this.invoiceNumber = invoiceNumber;
        this.accessKey = accessKey;
    }

    public void replaceItems(List<InvoiceItem> newItems) {
        items.clear();
        newItems.forEach(item -> item.setInvoice(this));
        items.addAll(newItems);
        recalculateTotals();
    }

    private void recalculateTotals() {
        totalAmount = sum(InvoiceItem::getSubtotal);
        totalIcms = sum(InvoiceItem::getIcmsAmount);
        totalPis = sum(InvoiceItem::getPisAmount);
        totalCofins = sum(InvoiceItem::getCofinsAmount);
    }

    private BigDecimal sum(java.util.function.Function<InvoiceItem, BigDecimal> extractor) {
        return items.stream().map(extractor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
