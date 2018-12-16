CREATE SEQUENCE hibernate_sequence START WITH 1 INCREMENT BY 1;
CREATE TABLE registered_names (
    name character varying(127) NOT NULL,
    generation integer NOT NULL,
    updating_key bytea NOT NULL,
    created timestamp without time zone NOT NULL,
    deadline timestamp without time zone NOT NULL,
    node_uri character varying(255) NOT NULL,
    PRIMARY KEY (name, generation)
);
CREATE TABLE signing_keys (
    id bigint NOT NULL PRIMARY KEY,
    name character varying(127) NOT NULL,
    generation integer NOT NULL,
    signing_key bytea NOT NULL,
    valid_from timestamp without time zone NOT NULL,
    FOREIGN KEY (name, generation) REFERENCES registered_names (name, generation)
);
