ALTER TABLE operations ADD COLUMN previous_digest bytea;
ALTER TABLE registered_names ADD COLUMN digest bytea NOT NULL;
