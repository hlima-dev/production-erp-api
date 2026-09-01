CREATE TABLE vehicles (
    id           UUID PRIMARY KEY,
    plate        VARCHAR(10)   NOT NULL UNIQUE,
    model        VARCHAR(255)  NOT NULL,
    capacity_kg  NUMERIC(10,2),
    active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE drivers (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    document    VARCHAR(20)   NOT NULL UNIQUE,
    license     VARCHAR(20)   NOT NULL UNIQUE,
    phone       VARCHAR(30),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE SEQUENCE delivery_manifest_number_seq START WITH 1;

CREATE TABLE delivery_manifests (
    id              UUID PRIMARY KEY,
    manifest_number BIGINT        NOT NULL UNIQUE,
    vehicle_id      UUID          NOT NULL REFERENCES vehicles(id),
    driver_id       UUID          NOT NULL REFERENCES drivers(id),
    status          VARCHAR(20)   NOT NULL DEFAULT 'PLANEJADO',
    departed_at     TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    notes           VARCHAR(500),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_delivery_manifests_status ON delivery_manifests(status);

-- Um pedido só pode estar em um romaneio (unique em order_id).
CREATE TABLE delivery_manifest_orders (
    manifest_id UUID NOT NULL REFERENCES delivery_manifests(id) ON DELETE CASCADE,
    order_id    UUID NOT NULL UNIQUE REFERENCES orders(id),
    PRIMARY KEY (manifest_id, order_id)
);
