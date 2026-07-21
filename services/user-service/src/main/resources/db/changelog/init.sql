CREATE SCHEMA IF NOT EXISTS rcrs_user;
SET search_path TO rcrs_user;

CREATE TABLE IF NOT EXISTS users (
    id           uuid PRIMARY KEY,
    keycloak_id  text NOT NULL UNIQUE,
    username     text NOT NULL,
    email        text,
    first_name   text,
    last_name    text,
    created_at   timestamp with time zone NOT NULL DEFAULT now(),
    updated_at   timestamp with time zone NOT NULL DEFAULT now()
);
