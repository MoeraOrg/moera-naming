CREATE TABLE operations (
    id uuid NOT NULL PRIMARY KEY,
    name character varying(127) NOT NULL,
    new_generation boolean NOT NULL,
    node_uri character varying(255),
    signature bytea,
    updating_key bytea,
    signing_key bytea,
    valid_from timestamp without time zone,
    status smallint NOT NULL,
    added timestamp without time zone NOT NULL,
    completed timestamp without time zone,
    error_code character varying(31),
    generation integer
);
CREATE INDEX ON operations(status);
CREATE INDEX ON operations(added);
CREATE INDEX ON operations(completed);
