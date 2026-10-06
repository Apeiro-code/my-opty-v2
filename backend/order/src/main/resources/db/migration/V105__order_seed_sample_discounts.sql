-- Sample discount data
-- Band: V100-V199
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- Conventions: InnoDB, utf8mb4, id BIGINT AUTO_INCREMENT, created_at/updated_at on
-- DATETIME(6), money DECIMAL(12,2), foreign keys ON DELETE RESTRICT unless stated.
--
-- Seed rows so the team can build and demo features against realistic data instead
-- of an empty table. Four discounts cover the states a feature has to handle: active
-- sitewide, active targeted, already ended, and not yet started.
--
-- Dates are relative to CURDATE() so the sample stays meaningful whenever the
-- migration runs. `target_item_ids` is built from subqueries on model and name
-- rather than hardcoded ids: the frames and lenses live in V7 (catalog band) and
-- their AUTO_INCREMENT ids are not this module's to assume. `created_by` stays
-- NULL — the column is nullable and an app_user row would mean inventing a
-- password hash before the authentication story exists.

INSERT INTO discount (description, percentage, target_item_ids, valid_from, valid_to,
                      is_active, ended_at, created_by)
SELECT 'New season: 10% off everything',
       10.00,
       NULL,
       CURDATE() - INTERVAL 7 DAY,
       CURDATE() + INTERVAL 60 DAY,
       TRUE,
       NULL,
       NULL;

INSERT INTO discount (description, percentage, target_item_ids, valid_from, valid_to,
                      is_active, ended_at, created_by)
SELECT 'Progressive lens event: 15% off selected lenses and frames',
       15.00,
       (SELECT JSON_ARRAYAGG(id) FROM (
            SELECT id FROM lens WHERE type = 'PROGRESSIVE'
            UNION ALL
            SELECT id FROM frame WHERE model IN ('Meridian 12', 'Astra 2100')
       ) AS targeted),
       CURDATE() - INTERVAL 3 DAY,
       CURDATE() + INTERVAL 30 DAY,
       TRUE,
       NULL,
       NULL;

INSERT INTO discount (description, percentage, target_item_ids, valid_from, valid_to,
                      is_active, ended_at, created_by)
SELECT 'Ramadan sale (ended): 20% off sitewide',
       20.00,
       NULL,
       CURDATE() - INTERVAL 90 DAY,
       CURDATE() - INTERVAL 60 DAY,
       FALSE,
       CURDATE() - INTERVAL 60 DAY,
       NULL;

INSERT INTO discount (description, percentage, target_item_ids, valid_from, valid_to,
                      is_active, ended_at, created_by)
SELECT 'Black Friday preview: 25% off sitewide',
       25.00,
       NULL,
       CURDATE() + INTERVAL 30 DAY,
       CURDATE() + INTERVAL 45 DAY,
       TRUE,
       NULL,
       NULL;
