-- Discount rules
-- Band: V100-V199
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- `target_item_ids` is JSON rather than a join table: a bulk discount names items
-- across two product tables (frame ids and lens ids), and one column holds that
-- without a polymorphic association table. The cost is that the database cannot
-- check the ids, which is the trade the bulk requirement asks for.

CREATE TABLE discount (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    description       VARCHAR(500)  NOT NULL,
    percentage        DECIMAL(5,2)  NOT NULL,
    target_item_ids   JSON          NULL,
    valid_from        DATE          NOT NULL,
    valid_to          DATE          NOT NULL,
    is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
    ended_at          DATETIME(6)   NULL,
    created_by        BIGINT        NULL,
    created_at        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_discount_validity (valid_from, valid_to),
    KEY idx_discount_active (is_active),
    KEY idx_discount_created_by (created_by),
    CONSTRAINT fk_discount_created_by
        FOREIGN KEY (created_by) REFERENCES app_user (id),
    CONSTRAINT chk_discount_percentage
        CHECK (percentage > 0 AND percentage <= 100),
    CONSTRAINT chk_discount_validity
        CHECK (valid_to >= valid_from)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
