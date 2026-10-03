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

CREATE TABLE monthly_report (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    report_month       DATE          NOT NULL,
    total_orders       INT           NOT NULL DEFAULT 0,
    total_revenue      DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_stock_added  INT           NOT NULL DEFAULT 0,
    total_stock_sold   INT           NOT NULL DEFAULT 0,
    new_customers      INT           NOT NULL DEFAULT 0,
    detail             JSON          NULL,
    generated_by       BIGINT        NULL,
    created_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_monthly_report_month (report_month),
    KEY idx_monthly_report_generated_by (generated_by),
    CONSTRAINT fk_monthly_report_generated_by
        FOREIGN KEY (generated_by) REFERENCES app_user (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
