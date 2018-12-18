ALTER TABLE signing_keys
    DROP CONSTRAINT signing_keys_name_fkey;
ALTER TABLE signing_keys
    ADD CONSTRAINT signing_keys_name_fkey
        FOREIGN KEY (name, generation)
        REFERENCES registered_names(name, generation)
        ON UPDATE CASCADE
        ON DELETE CASCADE;
