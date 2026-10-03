-- What the shop told a customer about an order
-- Band: V100-V199
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- `message` is stored as it was written. Rewording a template must not change what
-- a past notification claims was said, so the text is a copy and not a lookup
-- into a template table.

CREATE TABLE order_notification (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    order_id     BIGINT      NOT NULL,
    customer_id  BIGINT      NOT NULL,
    channel      VARCHAR(32) NOT NULL DEFAULT 'EMAIL',
    message      TEXT        NOT NULL,
    status       VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    sent_at      DATETIME(6) NULL,
    created_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_order_notification_order (order_id),
    KEY idx_order_notification_customer (customer_id),
    KEY idx_order_notification_sent_at (sent_at),
    CONSTRAINT fk_order_notification_order
        FOREIGN KEY (order_id) REFERENCES progressive_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_notification_customer
        FOREIGN KEY (customer_id) REFERENCES app_user (id),
    CONSTRAINT chk_order_notification_status
        CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
