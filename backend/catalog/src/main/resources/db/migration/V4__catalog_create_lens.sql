-- Lenses
-- Band: V2-V99
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- `type` drives order routing: a PROGRESSIVE lens is what puts an order into the
-- client's review queue, so it is a constrained value rather than free text.

CREATE TABLE lens (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    category_id         BIGINT        NULL,
    name                VARCHAR(160)  NOT NULL,
    type                VARCHAR(32)   NOT NULL,
    coating             VARCHAR(120)  NULL,
    price               DECIMAL(12,2) NOT NULL,
    stock_qty           INT           NOT NULL DEFAULT 0,
    low_stock_threshold INT           NOT NULL DEFAULT 5,
    description         TEXT          NULL,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_lens_category (category_id),
    KEY idx_lens_type (type),
    KEY idx_lens_price (price),
    KEY idx_lens_active (is_active),
    CONSTRAINT fk_lens_category
        FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT chk_lens_type
        CHECK (type IN ('SINGLE_VISION', 'BIFOCAL', 'PROGRESSIVE')),
    CONSTRAINT chk_lens_price
        CHECK (price >= 0),
    CONSTRAINT chk_lens_stock_qty
        CHECK (stock_qty >= 0)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
