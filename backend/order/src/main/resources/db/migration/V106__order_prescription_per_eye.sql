-- Per-eye cylinder, axis and add power
-- Band: V100-V199
--
-- Flyway is the only thing that writes DDL. Never edit a merged migration: the
-- database has already run it, so add a new one instead.
--
-- V100 gave sphere both eyes but left cylinder, axis and add power as one shared
-- value. A prescription is read off a form that has a column per eye for all
-- four, so a single shared value silently loses the difference between them —
-- exactly the kind of transcription error the VARCHAR columns exist to avoid.
--
-- The three shared columns are dropped, and it is safe to do so: no endpoint has
-- ever inserted a prescription (the order module ships no code until this
-- change), so `prescription` is empty on every environment. This is the one
-- destructive step in this migration and it deletes no data that exists.
--
-- Optical values stay VARCHAR, not DECIMAL, for the reason V100 gives: they are
-- transcribed as the optician wrote them ("+06.25").

ALTER TABLE prescription
    ADD COLUMN cyl_left       VARCHAR(16) NULL AFTER sph_right,
    ADD COLUMN cyl_right      VARCHAR(16) NULL AFTER cyl_left,
    ADD COLUMN axis_left      VARCHAR(16) NULL AFTER cyl_right,
    ADD COLUMN axis_right     VARCHAR(16) NULL AFTER axis_left,
    ADD COLUMN add_power_left VARCHAR(16) NULL AFTER axis_right,
    ADD COLUMN add_power_right VARCHAR(16) NULL AFTER add_power_left,
    DROP COLUMN cyl,
    DROP COLUMN axis,
    DROP COLUMN add_power;
