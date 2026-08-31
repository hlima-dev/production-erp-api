CREATE TABLE users (
    id                     UUID PRIMARY KEY,
    name                   VARCHAR(150) NOT NULL,
    email                  VARCHAR(255) NOT NULL UNIQUE,
    password               VARCHAR(255) NOT NULL,
    role                   VARCHAR(20)  NOT NULL,
    active                 BOOLEAN      NOT NULL DEFAULT TRUE,
    refresh_token          TEXT,
    failed_login_attempts  INT          NOT NULL DEFAULT 0,
    locked_until           TIMESTAMPTZ,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Usuário administrador padrão — senha "Admin@123", troque em produção.
INSERT INTO users (id, name, email, password, role)
VALUES (
    gen_random_uuid(),
    'Administrador',
    'admin@erp.com.br',
    '$2a$10$P7IDLYoIoqnw6aVaIJLnHOwBx072H3mp6zKujRdlomoeXLB7jQHoK',
    'ADMIN'
);
