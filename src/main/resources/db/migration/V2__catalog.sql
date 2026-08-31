CREATE TABLE products (
    id          UUID PRIMARY KEY,
    code        VARCHAR(40)   NOT NULL UNIQUE,
    name        VARCHAR(255)  NOT NULL,
    unit        VARCHAR(10)   NOT NULL,
    type        VARCHAR(20)   NOT NULL,
    category    VARCHAR(100),
    price       NUMERIC(12,2),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_type ON products(type);

CREATE TABLE bill_of_materials (
    id          UUID PRIMARY KEY,
    product_id  UUID          NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    notes       VARCHAR(500),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE bill_of_material_items (
    id                    UUID PRIMARY KEY,
    bill_of_material_id   UUID          NOT NULL REFERENCES bill_of_materials(id) ON DELETE CASCADE,
    ingredient_id         UUID          NOT NULL REFERENCES products(id),
    quantity_per_unit      NUMERIC(14,6) NOT NULL,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_bom_items_bom_id ON bill_of_material_items(bill_of_material_id);
