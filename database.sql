DROP DATABASE IF EXISTS warehouse_replenishment;

CREATE DATABASE warehouse_replenishment
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE warehouse_replenishment;

CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    unit VARCHAR(20) NOT NULL
);

CREATE TABLE supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    contact_email VARCHAR(160) NOT NULL,
    lead_time_days INT NOT NULL,
    reliability DOUBLE NOT NULL
);

CREATE TABLE stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity DOUBLE NOT NULL,
    min_level DOUBLE NOT NULL,
    daily_consumption DOUBLE NOT NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE replenishment_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    ordered_quantity DOUBLE NOT NULL,
    state VARCHAR(20) NOT NULL,
    f1 DOUBLE NOT NULL,
    f2 DOUBLE NOT NULL,
    f3 DOUBLE NOT NULL,
    priority_index DOUBLE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stock_id) REFERENCES stock(id),
    FOREIGN KEY (supplier_id) REFERENCES supplier(id)
);

INSERT INTO product (sku, name, unit) VALUES
('MLK-001', 'Молоко 2,5% 1 л', 'шт'),
('FLR-001', 'Борошно пшеничне 1 кг', 'шт'),
('SUG-001', 'Цукор 1 кг', 'шт');

INSERT INTO supplier (name, contact_email, lead_time_days, reliability) VALUES
('ТОВ «Агро-Захід»', 'sales@agro-zakhid.example', 3, 85.0),
('ПП «Південь-Постач»', 'order@pivden-postach.example', 7, 60.0);

INSERT INTO stock (product_id, quantity, min_level, daily_consumption, recorded_at) VALUES
(1, 120.0, 150.0, 40.0, '2026-11-01 09:00:00'),
(2, 500.0, 300.0, 55.0, '2026-11-01 09:00:00'),
(3, 35.0, 100.0, 20.0, '2026-11-01 09:00:00');
