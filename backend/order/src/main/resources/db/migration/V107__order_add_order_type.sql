-- The lens type the customer chose, snapshotted on the order
-- Band: V100-V199
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- `order_type` is copied from the chosen lens's type at creation rather than
-- joined back to `lens` on every routing decision. The workflow queue and the
-- reports read an order without reaching into the catalog module's tables, which
-- is the cross-module rule that matters: the order owns its routing key.
--
-- The values mirror `lens.type` (V4) and the `OrderType` enum. The column is
-- added NOT NULL with no default because the table is empty until the create
-- story lands; the application always supplies a value.

ALTER TABLE progressive_order
    ADD COLUMN order_type VARCHAR(32) NOT NULL AFTER lens_id,
    ADD KEY idx_progressive_order_type (order_type),
    ADD CONSTRAINT chk_progressive_order_type
        CHECK (order_type IN ('SINGLE_VISION', 'BIFOCAL', 'PROGRESSIVE'));
