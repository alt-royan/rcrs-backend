CREATE SCHEMA IF NOT EXISTS rcrs_user;
SET search_path TO rcrs_user;

CREATE TABLE IF NOT EXISTS users
(
    id         uuid PRIMARY KEY,
    username   text                     NOT NULL,
    avatar_key text,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    updated_at timestamp with time zone NOT NULL DEFAULT now()
);
