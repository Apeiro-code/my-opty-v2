-- Two demo accounts, because access control cannot be exercised before
-- registration (Epic 2) exists: without a row to log in as, the login endpoint
-- and the CUSTOMER/CLIENT split have nothing to run against.
--
-- The hashes are BCrypt (cost 10) of the passwords documented in docs/SETUP.md
-- and are constants — the salt lives inside the hash, so this file has no
-- randomness and Flyway still treats it as repeatable. Dev-only credentials:
-- these two rows are the first thing any deployment should replace.

INSERT INTO app_user (email, phone, password_hash, full_name, role, status, email_verified, phone_verified)
VALUES ('customer@myopty.local', NULL, '$2a$10$OAQtSCnjoITRcDYya04YVupBWqJeS76elbImEGxwfRsqsEHTvfHiu', 'Demo Customer', 'CUSTOMER', 'ACTIVE', TRUE, FALSE),
       ('client@myopty.local', NULL, '$2a$10$UFBXiyFvFcE4UXC8fOxQKuiWATrpja/2LaKVTyfblMcvs8wFJxwxO', 'Demo Shop Client', 'CLIENT', 'ACTIVE', TRUE, FALSE);
