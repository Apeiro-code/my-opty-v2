-- Frame and lens dealers
-- Band: V200-V299
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

CREATE TABLE dealer (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(160) NOT NULL,
    type        VARCHAR(32)  NOT NULL,
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(32)  NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_dealer_type (type),
    KEY idx_dealer_active (is_active),
    CONSTRAINT chk_dealer_type
        CHECK (type IN ('FRAME', 'LENS', 'BOTH'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
