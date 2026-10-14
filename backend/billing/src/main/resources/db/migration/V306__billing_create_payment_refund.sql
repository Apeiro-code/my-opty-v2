-- Refunds issued against a payment
-- Band: V300-V399
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- One row per refund, not a single reason column on `payment`: a payment may be
-- refunded more than once (a partial now, the rest later), and each refund has
-- its own amount, reason, who approved it and its gateway reference. Keeping the
-- history here is what lets `payment.refunded_amount` be a running total it can
-- be checked against. `refunded_by` is nullable because a refund may be issued
-- before the actor is recorded or by the system, and `gateway_refund_id` is the
-- gateway's own handle for the reversal, kept so a support query can be traced.

CREATE TABLE payment_refund (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    payment_id          BIGINT        NOT NULL,
    amount              DECIMAL(12,2) NOT NULL,
    reason              VARCHAR(500)  NULL,
    gateway_refund_id   VARCHAR(160)  NULL,
    refunded_by         BIGINT        NULL,
    created_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_payment_refund_payment (payment_id),
    KEY idx_payment_refund_refunded_by (refunded_by),
    CONSTRAINT fk_payment_refund_payment
        FOREIGN KEY (payment_id) REFERENCES payment (id),
    CONSTRAINT fk_payment_refund_refunded_by
        FOREIGN KEY (refunded_by) REFERENCES app_user (id),
    CONSTRAINT chk_payment_refund_amount
        CHECK (amount > 0)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
