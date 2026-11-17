-- Indexes for foreign keys and lookups that PostgreSQL does not index by itself.
-- tovar.regal_id is already covered by the unique (regal_id, slot) index and
-- pracovnik.drzany_tovar_id by its unique constraint.

-- Racks and workers of a room (dashboards, free slot lookup).
CREATE INDEX regal_miestnost_idx ON regal (miestnost_kluc);
CREATE INDEX pracovnik_miestnost_idx ON pracovnik (miestnost_kluc);

-- Goods waiting for a customer (customer page, pickup).
CREATE INDEX tovar_prijemca_idx ON tovar (prijemca_id);

-- Recovery links of a customer (rate limit check, invalidation) and expiry cleanup.
CREATE INDEX obnova_hesla_zakaznik_idx ON obnova_hesla (zakaznik_id, vytvorene);
CREATE INDEX obnova_hesla_plati_do_idx ON obnova_hesla (plati_do);

-- Audit filtered by event type, newest first.
CREATE INDEX audit_zaznam_akcia_cas_idx ON audit_zaznam (akcia, cas DESC);

-- Expired login counters cleanup.
CREATE INDEX prihlasenie_pokusy_zaciatok_idx ON prihlasenie_pokusy (zaciatok);
