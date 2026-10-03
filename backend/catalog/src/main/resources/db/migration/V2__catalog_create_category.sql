-- Browsing categories
-- Band: V2-V99
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- `parent_id` makes the hierarchy self-referencing, which is what lets a category
-- tree grow without a second table. `item_type` says whether a category holds
-- frames, lenses, or both.

CREATE TABLE category (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(120) NOT NULL,
    slug        VARCHAR(120) NOT NULL,
    parent_id   BIGINT       NULL,
    item_type   VARCHAR(32)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_category_slug (slug),
    KEY idx_category_parent (parent_id),
    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id) REFERENCES category (id),
    CONSTRAINT chk_category_item_type
        CHECK (item_type IN ('FRAME', 'LENS', 'BOTH'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
