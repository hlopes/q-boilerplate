-- =============================================================
-- V1__initial_schema.sql
-- Initial schema: tenants, users and products tables
-- =============================================================

-- -------------------------------------------------------
-- Tenants
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS tenants (
    id               UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    domain           VARCHAR(255) UNIQUE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_tenants_domain ON tenants (domain);
CREATE INDEX IF NOT EXISTS idx_tenants_status ON tenants (status);

-- -------------------------------------------------------
-- Users
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id               UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id        UUID         NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    first_name       VARCHAR(100),
    last_name        VARCHAR(100),
    role             VARCHAR(20)  NOT NULL DEFAULT 'USER',
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    picture          VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_status   ON users (status);
CREATE INDEX IF NOT EXISTS idx_users_tenant   ON users (tenant_id);

-- -------------------------------------------------------
-- Products
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id              UUID           DEFAULT gen_random_uuid() PRIMARY KEY,
    name            VARCHAR(255)   NOT NULL,
    description     TEXT,
    sku             VARCHAR(100)   NOT NULL UNIQUE,
    price           NUMERIC(12, 2) NOT NULL,
    stock_quantity  INTEGER        NOT NULL DEFAULT 0,
    category        VARCHAR(100),
    status          VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_products_sku      ON products (sku);
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category);
CREATE INDEX IF NOT EXISTS idx_products_status   ON products (status);
