-- Dealer stock-request emails
-- Band: V200-V299
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- The client approves before anything is sent, so the row is created as DRAFT and
-- `approved_at` records that it happened. An auto-generated email is kept, flagged
-- and rewritable, because the client is the one who sends it.

CREATE TABLE dealer_email (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    dealer_id      BIGINT       NOT NULL,
    client_id      BIGINT       NOT NULL,
    subject        VARCHAR(255) NOT NULL,
    body           TEXT         NOT NULL,
    status         VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',
    auto_generated  BOOLEAN     NOT NULL DEFAULT FALSE,
    approved_at    DATETIME(6)  NULL,
    sent_at        DATETIME(6)  NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_dealer_email_dealer (dealer_id),
    KEY idx_dealer_email_client (client_id),
    KEY idx_dealer_email_status (status),
    CONSTRAINT fk_dealer_email_dealer
        FOREIGN KEY (dealer_id) REFERENCES dealer (id),
    CONSTRAINT fk_dealer_email_client
        FOREIGN KEY (client_id) REFERENCES app_user (id),
    CONSTRAINT chk_dealer_email_status
        CHECK (status IN ('DRAFT', 'SENT', 'REPLIED', 'FULFILLED'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
