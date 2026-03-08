-- =============================================================
-- V1__initial_schema.sql
-- Initial schema: users and products tables
-- =============================================================

-- -------------------------------------------------------
-- Users
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id               BIGSERIAL PRIMARY KEY,
    username         VARCHAR(50)  NOT NULL UNIQUE,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    first_name       VARCHAR(100),
    last_name        VARCHAR(100),
    role             VARCHAR(20)  NOT NULL DEFAULT 'USER',
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_users_email    ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_status   ON users (status);

-- -------------------------------------------------------
-- Products
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id              BIGSERIAL PRIMARY KEY,
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
