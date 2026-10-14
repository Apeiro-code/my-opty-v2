-- Seed the accepted payment methods
-- Band: V300-V399
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- V300 creates `payment_method` empty; the checkout has nothing to offer until
-- the methods it may choose from exist. The three codes are the ones the V300
-- check constraint already permits and the ones the task names (card, bank
-- transfer, cash on pickup), so this migration only adds the rows — the shape of
-- the method stays a DDL concern.
--
-- `provider_config` stays NULL: it is the gateway's credentials and the local
-- `fake` provider needs none. `sort_order` is spaced by ten so a method can be
-- slotted between two without renumbering. The inserts are guarded on `code`
-- (unique in V300) so re-running against a database where the rows already exist
-- is a no-op rather than a duplicate-key failure.

INSERT INTO payment_method (code, display_name, sort_order) VALUES
    ('CARD', 'Credit or debit card', 10),
    ('BANK_TRANSFER', 'Bank transfer', 20),
    ('CASH_ON_PICKUP', 'Cash on pickup', 30)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    sort_order = VALUES(sort_order);
