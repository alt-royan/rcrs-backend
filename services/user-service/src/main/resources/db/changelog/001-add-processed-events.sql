
SET search_path TO rcrs_user;

CREATE TABLE IF NOT EXISTS processed_events
(
    event_id     text PRIMARY KEY,
    event_type   text                     NOT NULL,
    processed_at timestamp with time zone NOT NULL DEFAULT now()
);
