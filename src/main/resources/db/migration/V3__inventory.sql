CREATE TABLE warehouses (
    id          UUID PRIMARY KEY,
    code        VARCHAR(20)   NOT NULL UNIQUE,
    name        VARCHAR(255)  NOT NULL,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

INSERT INTO warehouses (id, code, name) VALUES (gen_random_uuid(), 'CD', 'CD Fábrica');

CREATE TABLE stock_items (
    id           UUID PRIMARY KEY,
    product_id   UUID          NOT NULL REFERENCES products(id),
    warehouse_id UUID          NOT NULL REFERENCES warehouses(id),
    quantity     NUMERIC(14,6) NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    UNIQUE (product_id, warehouse_id)
);

CREATE TABLE stock_movements (
    id              UUID PRIMARY KEY,
    product_id      UUID          NOT NULL REFERENCES products(id),
    warehouse_id    UUID          NOT NULL REFERENCES warehouses(id),
    type            VARCHAR(20)   NOT NULL,
    quantity        NUMERIC(14,6) NOT NULL,
    reference_type  VARCHAR(40),
    reference_id    UUID,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_reference ON stock_movements(reference_type, reference_id);
