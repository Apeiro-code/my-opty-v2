-- Customer prescriptions
-- Band: V100-V199
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- Optical values are VARCHAR, not DECIMAL: they are written as the optician wrote
-- them ("+06.25", "0.00 x 180") and are transcribed verbatim into production. Parsing
-- them into numbers here would quietly change a value that was read off a scan.
--
-- `document_object_key` is a key, not a path. The bytes live in the object store
-- (MINIO_* in .env); this table holds only the metadata needed to fetch them.

CREATE TABLE prescription (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    customer_id           BIGINT       NOT NULL,
    sph_left              VARCHAR(16)  NULL,
    sph_right             VARCHAR(16)  NULL,
    cyl                   VARCHAR(16)  NULL,
    axis                  VARCHAR(16)  NULL,
    add_power             VARCHAR(16)  NULL,
    is_progressive        BOOLEAN      NOT NULL DEFAULT FALSE,
    document_object_key   VARCHAR(512) NULL,
    document_content_type VARCHAR(120) NULL,
    verification_status   VARCHAR(32)  NOT NULL DEFAULT 'PENDING_REVIEW',
    rejection_reason      VARCHAR(500) NULL,
    verified_by           BIGINT       NULL,
    verified_at           DATETIME(6)  NULL,
    created_at            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    KEY idx_prescription_customer (customer_id),
    KEY idx_prescription_status (verification_status),
    KEY idx_prescription_verified_by (verified_by),
    CONSTRAINT fk_prescription_customer
        FOREIGN KEY (customer_id) REFERENCES app_user (id),
    CONSTRAINT fk_prescription_verified_by
        FOREIGN KEY (verified_by) REFERENCES app_user (id),
    CONSTRAINT chk_prescription_verification_status
        CHECK (verification_status IN ('PENDING_REVIEW', 'VERIFIED', 'REJECTED'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
