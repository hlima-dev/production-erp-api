package com.hlima.erp.fiscal.entity;

import com.hlima.erp.catalog.entity.Product;
import com.hlima.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 14, scale = 6)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    // CFOP fixo (5102 — venda de mercadoria dentro do estado) por simplificação:
    // numa NF-e real varia por operação/UF de destino.
    @Column(nullable = false, length = 10)
    private String cfop;

    @Column(name = "icms_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal icmsRate;

    @Column(name = "icms_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal icmsAmount;

    @Column(name = "pis_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal pisRate;

    @Column(name = "pis_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal pisAmount;

    @Column(name = "cofins_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal cofinsRate;

    @Column(name = "cofins_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal cofinsAmount;

    public InvoiceItem(
            Product product, BigDecimal quantity, BigDecimal unitPrice, BigDecimal subtotal, String cfop,
            BigDecimal icmsRate, BigDecimal icmsAmount,
            BigDecimal pisRate, BigDecimal pisAmount,
            BigDecimal cofinsRate, BigDecimal cofinsAmount
    ) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.cfop = cfop;
        this.icmsRate = icmsRate;
        this.icmsAmount = icmsAmount;
        this.pisRate = pisRate;
        this.pisAmount = pisAmount;
        this.cofinsRate = cofinsRate;
        this.cofinsAmount = cofinsAmount;
    }
}
