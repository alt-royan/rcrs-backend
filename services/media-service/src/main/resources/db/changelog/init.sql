CREATE SCHEMA IF NOT EXISTS rcrs_media;
SET search_path TO rcrs_media;

CREATE TABLE IF NOT EXISTS audio_upload
(
    uid                text PRIMARY KEY,
    status             text                     NOT NULL,
    original_file_name text                     NOT NULL,
    track_id           text,
    content_length     bigint                   NOT NULL,
    content_type       varchar                  NOT NULL,
    expires_at         timestamp with time zone,
    created_at         timestamp with time zone NOT NULL DEFAULT now(),
    error              text
);

CREATE TABLE IF NOT EXISTS audio
(
    id                 uuid PRIMARY KEY,
    guid               uuid                     NOT NULL,
    key                text                     NOT NULL,
    codec              varchar                  NOT NULL,
    duration_ms        int                      NOT NULL,
    container          varchar                  NOT NULL,
    content_type       text                     NOT NULL,
    bitrate            varchar                  NOT NULL,
    quality            varchar                  NOT NULL,
    sample_rate        varchar                  NOT NULL,
    byte_size          bigint                   NOT NULL,
    creation_timestamp timestamp with time zone NOT NULL
);
