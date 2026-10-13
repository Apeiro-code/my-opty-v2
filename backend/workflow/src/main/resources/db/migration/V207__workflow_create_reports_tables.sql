-- Monthly sales and stock reports
-- Band: V200-V299
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- `report_month` is a DATE fixed to the first of the month and is unique, so
-- regenerating a month replaces the row instead of adding a second, slightly
-- different, version of the same report.

CREATE TABLE  report_sales_monthly (
    report_id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT,
    month INT NOT NULL,
    year INT NOT NULL,
    total_orders INT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES client (client_id)
);

CREATE TABLE  report_inventory_monthly (
    report_id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT,
    month INT NOT NULL,
    year INT NOT NULL,
    stock_added INT NOT NULL DEFAULT 0,
    stock_sold INT NOT NULL DEFAULT 0,
    current_stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES client (client_id)
);

CREATE TABLE  report_historical (
    historical_id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT,
    month INT,
    year INT,
    metric_type VARCHAR(50) NOT NULL,
    metric_value DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES client (client_id)
);

CREATE TABLE  dashboard_summary (
    summary_id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT,
    date DATE NOT NULL,
    total_sales DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_orders INT NOT NULL DEFAULT 0,
    low_stock_items INT NOT NULL DEFAULT 0,
    pending_tasks INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES client (client_id)
);
