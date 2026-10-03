-- Monthly billing reports
-- Band: V300-V399
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- One row per month, unique on the month, for the same reason as monthly_report:
-- regenerating replaces rather than accumulates.

CREATE TABLE billing_report (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    report_month        DATE          NOT NULL,
    gross_revenue       DECIMAL(14,2) NOT NULL DEFAULT 0,
    refunds_total       DECIMAL(14,2) NOT NULL DEFAULT 0,
    net_revenue         DECIMAL(14,2) NOT NULL DEFAULT 0,
    payment_count       INT           NOT NULL DEFAULT 0,
    breakdown_by_method JSON          NULL,
    generated_by        BIGINT        NULL,
    created_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_billing_report_month (report_month),
    KEY idx_billing_report_generated_by (generated_by),
    CONSTRAINT fk_billing_report_generated_by
        FOREIGN KEY (generated_by) REFERENCES app_user (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
