-- Accepted payment methods
-- Band: V300-V399
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- `provider_config` is JSON because each method carries different credentials and
-- the shape is the gateway's, not ours. It stays NULL while the local fake gateway
-- is the configured provider, which needs none.

CREATE TABLE payment_method (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    code            VARCHAR(32)   NOT NULL,
    display_name    VARCHAR(120)  NOT NULL,
    provider_config JSON          NULL,
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    sort_order      INT           NOT NULL DEFAULT 0,
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_method_code (code),
    KEY idx_payment_method_active (is_active, sort_order),
    CONSTRAINT chk_payment_method_code
        CHECK (code IN ('CARD', 'BANK_TRANSFER', 'CASH_ON_PICKUP'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
