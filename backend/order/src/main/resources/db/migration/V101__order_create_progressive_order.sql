-- Orders built against a prescription
-- Band: V100-V199
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- The frame and lens sit on the order rather than in a separate order_item table:
-- an optical order is one frame and one lens, so a line-item table would have
-- exactly one row per order and carry no information.
--
-- `frame_id` is nullable because a customer may order lenses only.
--
-- Foreign keys reach into app_user and the catalog module's tables. That is a
-- database-level constraint, not a Java dependency: `order` still cannot compile
-- against `com.myopty.catalog.model`, which is the rule that matters here.

CREATE TABLE progressive_order (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    order_number      VARCHAR(40)   NOT NULL,
    customer_id       BIGINT        NOT NULL,
    prescription_id   BIGINT        NOT NULL,
    frame_id          BIGINT        NULL,
    lens_id           BIGINT        NOT NULL,
    quantity          INT           NOT NULL DEFAULT 1,
    order_date        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    receive_date      DATE          NULL,
    status            VARCHAR(32)   NOT NULL DEFAULT 'PENDING',
    total_amount      DECIMAL(12,2) NULL,
    rejection_reason  VARCHAR(500)  NULL,
    created_at        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_progressive_order_number (order_number),
    KEY idx_progressive_order_customer (customer_id),
    KEY idx_progressive_order_prescription (prescription_id),
    KEY idx_progressive_order_frame (frame_id),
    KEY idx_progressive_order_lens (lens_id),
    KEY idx_progressive_order_status (status),
    KEY idx_progressive_order_status_date (status, order_date),
    CONSTRAINT fk_progressive_order_customer
        FOREIGN KEY (customer_id) REFERENCES app_user (id),
    CONSTRAINT fk_progressive_order_prescription
        FOREIGN KEY (prescription_id) REFERENCES prescription (id),
    CONSTRAINT fk_progressive_order_frame
        FOREIGN KEY (frame_id) REFERENCES frame (id),
    CONSTRAINT fk_progressive_order_lens
        FOREIGN KEY (lens_id) REFERENCES lens (id),
    CONSTRAINT chk_progressive_order_quantity
        CHECK (quantity > 0),
    CONSTRAINT chk_progressive_order_status
        CHECK (status IN ('PENDING', 'APPROVED', 'PROCESSING', 'READY', 'DISPATCHED', 'REJECTED'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
