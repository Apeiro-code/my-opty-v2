-- Email verification OTP storage
-- Band: V1
CREATE TABLE email_verification (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    user_id         BIGINT        NOT NULL,
    purpose         VARCHAR(32)   NOT NULL,
    code_hash       VARCHAR(255)  NOT NULL,
    expires_at      DATETIME(6)   NOT NULL,
    attempts        INT           NOT NULL DEFAULT 0,
    used            BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_ev_user_purpose (user_id, purpose),
    CONSTRAINT fk_ev_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT chk_ev_purpose CHECK (purpose IN ('EMAIL_VERIFICATION'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
