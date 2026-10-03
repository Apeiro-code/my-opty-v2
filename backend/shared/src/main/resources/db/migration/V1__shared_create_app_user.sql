-- User accounts
-- Band: V1
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.

-- Customers and the shop owner are rows in this one table, separated by `role`.
-- Two tables, one per kind of person, was the alternative, and it fails at
-- authentication: a login has to resolve to one identity before it knows which
-- profile to load. `client_profile` and `customer_profile` carry the extras.

CREATE TABLE app_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255) NOT NULL,
    phone           VARCHAR(32)  NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    status          VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    phone_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_app_user_email (email),
    UNIQUE KEY uq_app_user_phone (phone),
    KEY idx_app_user_role (role),
    CONSTRAINT chk_app_user_role
        CHECK (role IN ('CUSTOMER', 'CLIENT')),
    CONSTRAINT chk_app_user_status
        CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'DISABLED'))
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
