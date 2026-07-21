--liquibase formatted sql

--changeset rcrs:003-add-processed-events
CREATE TABLE IF NOT EXISTS rcrs_user.processed_events (
    event_id    text PRIMARY KEY,
    event_type  text NOT NULL,
    processed_at timestamp with time zone NOT NULL DEFAULT now()
);
