CREATE TABLE customers (
    id          UUID PRIMARY KEY,
    document    VARCHAR(20)   NOT NULL UNIQUE,
    name        VARCHAR(255)  NOT NULL,
    email       VARCHAR(255),
    phone       VARCHAR(30),
    address     VARCHAR(500),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE SEQUENCE order_number_seq START WITH 1000;

CREATE TABLE orders (
    id            UUID PRIMARY KEY,
    customer_id   UUID          NOT NULL REFERENCES customers(id),
    order_number  BIGINT        NOT NULL UNIQUE,
    status        VARCHAR(20)   NOT NULL DEFAULT 'RASCUNHO',
    total_amount  NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE order_items (
    id          UUID PRIMARY KEY,
    order_id    UUID          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id  UUID          NOT NULL REFERENCES products(id),
    quantity    NUMERIC(14,6) NOT NULL,
    unit_price  NUMERIC(12,2) NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
