CREATE SEQUENCE invoice_number_seq START WITH 1;

CREATE TABLE invoices (
    id            UUID PRIMARY KEY,
    order_id      UUID          NOT NULL UNIQUE REFERENCES orders(id),
    invoice_number BIGINT       NOT NULL UNIQUE,
    access_key    VARCHAR(44)   NOT NULL UNIQUE,
    status        VARCHAR(20)   NOT NULL DEFAULT 'EMITIDA',
    total_amount  NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_icms    NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_pis     NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_cofins  NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoices_status ON invoices(status);

CREATE TABLE invoice_items (
    id            UUID PRIMARY KEY,
    invoice_id    UUID          NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_id    UUID          NOT NULL REFERENCES products(id),
    quantity      NUMERIC(14,6) NOT NULL,
    unit_price    NUMERIC(12,2) NOT NULL,
    subtotal      NUMERIC(12,2) NOT NULL,
    cfop          VARCHAR(10)   NOT NULL,
    icms_rate     NUMERIC(5,2)  NOT NULL,
    icms_amount   NUMERIC(12,2) NOT NULL,
    pis_rate      NUMERIC(5,2)  NOT NULL,
    pis_amount    NUMERIC(12,2) NOT NULL,
    cofins_rate   NUMERIC(5,2)  NOT NULL,
    cofins_amount NUMERIC(12,2) NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoice_items_invoice ON invoice_items(invoice_id);
