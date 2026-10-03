-- Payments against orders
-- Band: V300-V399
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- An order has at most one successful payment, but the table does not enforce
-- that: an authorisation that fails and is retried is a second row, and a UNIQUE
-- constraint here would block the retry that the status column exists to record.

CREATE TABLE payment (
    id                      BIGINT        NOT NULL AUTO_INCREMENT,
    order_id                BIGINT        NOT NULL,
    customer_id             BIGINT        NOT NULL,
    method_id               BIGINT        NOT NULL,
    amount                  DECIMAL(12,2) NOT NULL,
    status                  VARCHAR(32)   NOT NULL DEFAULT 'PENDING',
    gateway_transaction_id  VARCHAR(160)  NULL,
    refunded_amount         DECIMAL(12,2) NOT NULL DEFAULT 0,
    paid_at                 DATETIME(6)   NULL,
    created_at              DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_payment_order (order_id),
    KEY idx_payment_customer (customer_id),
    KEY idx_payment_status (status),
    KEY idx_payment_method (method_id),
    KEY idx_payment_gateway_transaction (gateway_transaction_id),
    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id) REFERENCES progressive_order (id),
    CONSTRAINT fk_payment_customer
        FOREIGN KEY (customer_id) REFERENCES app_user (id),
    CONSTRAINT fk_payment_method
        FOREIGN KEY (method_id) REFERENCES payment_method (id),
    CONSTRAINT chk_payment_amount
        CHECK (amount >= 0),
    CONSTRAINT chk_payment_status
        CHECK (status IN ('PENDING', 'AUTHORIZED', 'CAPTURED', 'REFUNDED', 'FAILED'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
