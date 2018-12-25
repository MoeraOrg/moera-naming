ALTER TABLE signing_keys ADD COLUMN created timestamp without time zone NOT NULL;
CREATE INDEX ON signing_keys(name, generation);
CREATE INDEX ON signing_keys(created);
