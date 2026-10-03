-- Stock movement audit log
-- Band: V2-V99
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- Deliberately no foreign key on (item_type, item_id). One audit table covering two
-- product tables cannot have a real FK, and a nullable `frame_id` / `lens_id` pair
-- with a CHECK to keep exactly one set would be the same restriction expressed
-- worse. `stock_after` is stored so a report can be produced without replaying the
-- whole log.

CREATE TABLE stock_entry (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    item_type        VARCHAR(32)  NOT NULL,
    item_id          BIGINT       NOT NULL,
    change_type      VARCHAR(32)  NOT NULL,
    quantity_change  INT          NOT NULL,
    stock_after      INT          NOT NULL,
    reference        VARCHAR(120) NULL,
    note             VARCHAR(500) NULL,
    created_by       BIGINT       NULL,
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_stock_entry_item (item_type, item_id),
    KEY idx_stock_entry_created_at (created_at),
    KEY idx_stock_entry_created_by (created_by),
    CONSTRAINT fk_stock_entry_created_by
        FOREIGN KEY (created_by) REFERENCES app_user (id),
    CONSTRAINT chk_stock_entry_item_type
        CHECK (item_type IN ('FRAME', 'LENS')),
    CONSTRAINT chk_stock_entry_change_type
        CHECK (change_type IN ('SALE', 'RETURN', 'NEW_STOCK', 'ADJUSTMENT'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
