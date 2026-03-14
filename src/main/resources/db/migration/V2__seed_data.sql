-- =============================================================
-- V2__seed_data.sql
-- Initial seed data for development / demo environments
-- =============================================================

-- Seed default tenant
INSERT INTO tenants (id, domain, status)
VALUES ('00000000-0000-0000-0000-000000000001', 'default.com', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- Admin user (password: admin123 — hashed placeholder)
INSERT INTO users (tenant_id, email, first_name, last_name, role, status)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin@example.com', 'Admin', 'User', 'ADMIN', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- Demo regular user
INSERT INTO users (tenant_id, email, first_name, last_name, role, status)
VALUES ('00000000-0000-0000-0000-000000000001', 'john.doe@example.com', 'John', 'Doe', 'USER', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- Sample products
INSERT INTO products (name, description, sku, price, stock_quantity, category, status)
VALUES
    ('Laptop Pro 15', 'High-performance laptop with 32GB RAM', 'LAPTOP-PRO-15', 1299.99, 50, 'Electronics', 'ACTIVE'),
    ('Wireless Keyboard', 'Ergonomic wireless keyboard', 'KEYB-WIRELESS-01', 79.99, 200, 'Accessories', 'ACTIVE'),
    ('USB-C Hub 7-in-1', '7-port USB-C hub with HDMI and SD card reader', 'HUB-USBC-7IN1', 49.99, 150, 'Accessories', 'ACTIVE'),
    ('4K Monitor 27"', '4K UHD IPS display 144Hz', 'MON-4K-27', 649.99, 30, 'Electronics', 'ACTIVE'),
    ('Mechanical Mouse', 'Gaming mouse with RGB lighting', 'MOUSE-MECH-RGB', 59.99, 5, 'Accessories', 'ACTIVE')
ON CONFLICT DO NOTHING;
