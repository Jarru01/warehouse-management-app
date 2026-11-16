-- Optimistic locking versions and the "must change password" flag.

ALTER TABLE riaditel
    ADD COLUMN verzia      BIGINT  NOT NULL DEFAULT 0,
    ADD COLUMN zmena_hesla BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE pracovnik
    ADD COLUMN verzia      BIGINT  NOT NULL DEFAULT 0,
    ADD COLUMN zmena_hesla BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE zakaznik
    ADD COLUMN verzia      BIGINT  NOT NULL DEFAULT 0,
    ADD COLUMN zmena_hesla BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE regal
    ADD COLUMN verzia BIGINT NOT NULL DEFAULT 0;

ALTER TABLE tovar
    ADD COLUMN verzia BIGINT NOT NULL DEFAULT 0;
