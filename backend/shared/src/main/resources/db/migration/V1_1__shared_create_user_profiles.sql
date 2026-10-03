-- Profile extensions for app_user
-- Band: V1 (nested version 1_1, ordered after V1)
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- Both profiles are one logical change: the per-role half of a single user
-- aggregate. Each is keyed by the user id, so creating a user and its profile is
-- one insert each and there is no second identity to keep in step.

CREATE TABLE client_profile (
    user_id      BIGINT       NOT NULL,
    shop_name    VARCHAR(255) NOT NULL,
    venue        VARCHAR(255) NOT NULL,
    contact_tel  VARCHAR(32)  NULL,
    address      TEXT         NULL,

    PRIMARY KEY (user_id),
    CONSTRAINT fk_client_profile_user
        FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE customer_profile (
    user_id            BIGINT      NOT NULL,
    address            TEXT        NULL,
    preferred_contact  VARCHAR(32) NULL,

    PRIMARY KEY (user_id),
    CONSTRAINT fk_customer_profile_user
        FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT chk_customer_profile_preferred_contact
        CHECK (preferred_contact IN ('EMAIL', 'PHONE', 'SMS'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;