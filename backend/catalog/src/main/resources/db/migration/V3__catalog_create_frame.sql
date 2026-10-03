-- Frames
-- Band: V2-V99
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- `stock_qty` and `low_stock_threshold` sit on the row so the availability report
-- is one query rather than a join against an audit table. `stock_entry` is the
-- history; this is the current number.

CREATE TABLE frame (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    category_id         BIGINT        NULL,
    model               VARCHAR(160)  NOT NULL,
    color               VARCHAR(120)  NULL,
    material            VARCHAR(120)  NULL,
    shape               VARCHAR(60)   NULL,
    price               DECIMAL(12,2) NOT NULL,
    stock_qty           INT           NOT NULL DEFAULT 0,
    low_stock_threshold INT           NOT NULL DEFAULT 5,
    image_url           VARCHAR(512)  NULL,
    description         TEXT          NULL,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_frame_category (category_id),
    KEY idx_frame_model (model),
    KEY idx_frame_price (price),
    KEY idx_frame_active (is_active),
    CONSTRAINT fk_frame_category
        FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT chk_frame_price
        CHECK (price >= 0),
    CONSTRAINT chk_frame_stock_qty
        CHECK (stock_qty >= 0)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
