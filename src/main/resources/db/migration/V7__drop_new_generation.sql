DELETE FROM operations;
ALTER TABLE operations DROP COLUMN new_generation;
ALTER TABLE operations ALTER COLUMN generation SET NOT NULL;
