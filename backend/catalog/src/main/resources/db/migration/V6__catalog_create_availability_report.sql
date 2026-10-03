-- Inventory availability snapshots
-- Band: V2-V99
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- A generated snapshot, not live state: the report is the answer as it stood at
-- `generated_at`, and the export is of what the client saw.

CREATE TABLE availability_report (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    generated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    generated_by      BIGINT      NULL,
    total_items       INT         NOT NULL DEFAULT 0,
    in_stock_count    INT         NOT NULL DEFAULT 0,
    low_stock_count   INT         NOT NULL DEFAULT 0,
    out_of_stock_count INT        NOT NULL DEFAULT 0,
    detail            JSON        NULL,
    created_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_availability_report_generated_at (generated_at),
    KEY idx_availability_report_generated_by (generated_by),
    CONSTRAINT fk_availability_report_generated_by
        FOREIGN KEY (generated_by) REFERENCES app_user (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
