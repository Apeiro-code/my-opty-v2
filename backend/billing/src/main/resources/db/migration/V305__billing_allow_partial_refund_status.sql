-- Allow a part-refunded payment to say so
-- Band: V300-V399
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- V301's `chk_payment_status` knows only PENDING, AUTHORIZED, CAPTURED, REFUNDED
-- and FAILED, so a payment refunded in part had to lie and read as fully
-- REFUNDED. The refund story needs the third state: `refunded_amount` already
-- records how much went back, and this lets `status` agree with it.
--
-- MySQL names the existing constraint, so it is dropped and re-added rather than
-- altered. The new set is a superset, so every existing row still satisfies it.

ALTER TABLE payment DROP CHECK chk_payment_status;

ALTER TABLE payment ADD CONSTRAINT chk_payment_status
    CHECK (status IN ('PENDING', 'AUTHORIZED', 'CAPTURED', 'PARTIALLY_REFUNDED', 'REFUNDED', 'FAILED'));
