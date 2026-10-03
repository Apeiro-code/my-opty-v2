-- Invoices issued against a payment
-- Band: V300-V399
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- The amounts are stored rather than recomputed from the order. An invoice is a
-- statement of what was charged at that moment, and re-deriving it later from a
-- changed order total would quietly rewrite history.

CREATE TABLE invoice (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    invoice_number   VARCHAR(40)   NOT NULL,
    payment_id       BIGINT        NOT NULL,
    order_id         BIGINT        NOT NULL,
    subtotal         DECIMAL(12,2) NOT NULL,
    discount_amount  DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount       DECIMAL(12,2) NOT NULL DEFAULT 0,
    total            DECIMAL(12,2) NOT NULL,
    pdf_object_key   VARCHAR(512)  NULL,
    issued_at        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_invoice_number (invoice_number),
    KEY idx_invoice_payment (payment_id),
    KEY idx_invoice_order (order_id),
    KEY idx_invoice_issued_at (issued_at),
    CONSTRAINT fk_invoice_payment
        FOREIGN KEY (payment_id) REFERENCES payment (id),
    CONSTRAINT fk_invoice_order
        FOREIGN KEY (order_id) REFERENCES progressive_order (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
