CREATE SEQUENCE production_order_number_seq START WITH 1;

CREATE TABLE production_orders (
    id                UUID PRIMARY KEY,
    order_number      BIGINT        NOT NULL UNIQUE,
    product_id        UUID          NOT NULL REFERENCES products(id),
    warehouse_id      UUID          NOT NULL REFERENCES warehouses(id),
    planned_quantity  NUMERIC(14,6) NOT NULL,
    produced_quantity NUMERIC(14,6),
    status            VARCHAR(20)   NOT NULL DEFAULT 'ABERTA',
    started_at        TIMESTAMPTZ,
    completed_at      TIMESTAMPTZ,
    notes             VARCHAR(500),
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_production_orders_status ON production_orders(status);
CREATE INDEX idx_production_orders_product ON production_orders(product_id);
