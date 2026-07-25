CREATE SCHEMA IF NOT EXISTS rcrs_user;
SET search_path TO rcrs_user;

CREATE TABLE IF NOT EXISTS users
(
    user_id        uuid PRIMARY KEY,
    username       text                     NOT NULL,
    email          text,
    enabled        boolean                  NOT NULL DEFAULT true,
    email_verified boolean                  NOT NULL DEFAULT false,
    created_at     timestamp with time zone NOT NULL DEFAULT now(),
    updated_at     timestamp with time zone NOT NULL DEFAULT now()
);
