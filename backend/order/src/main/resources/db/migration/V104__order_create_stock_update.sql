-- New stock received from a dealer
-- Band: V100-V199
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- Kept separate from catalog's `stock_entry`: this row is the purchase (what was
-- received, at what cost, from whom), and `stock_entry` is the movement it causes.
-- `item_type` / `item_id` is unconstrained for the same reason as in `stock_entry`.

CREATE TABLE stock_update (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    item_type    VARCHAR(32)   NOT NULL,
    item_id      BIGINT        NOT NULL,
    quantity     INT           NOT NULL,
    unit_cost    DECIMAL(12,2) NULL,
    supplier     VARCHAR(160)  NULL,
    received_by  BIGINT        NULL,
    received_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    note         VARCHAR(500)  NULL,
    created_at   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_stock_update_item (item_type, item_id),
    KEY idx_stock_update_received_at (received_at),
    KEY idx_stock_update_received_by (received_by),
    CONSTRAINT fk_stock_update_received_by
        FOREIGN KEY (received_by) REFERENCES app_user (id),
    CONSTRAINT chk_stock_update_item_type
        CHECK (item_type IN ('FRAME', 'LENS')),
    CONSTRAINT chk_stock_update_quantity
        CHECK (quantity > 0)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
