SET search_path TO rcrs_upload;

CREATE TABLE IF NOT EXISTS audio_upload
(
    uid                text PRIMARY KEY,
    status             text NOT NULL,
    original_file_name text NOT NULL,
    track_id           text,
    content_length     bigint,
    content_type       varchar(20),
    expires_at         timestamp,
    error              text
);

CREATE TABLE IF NOT EXISTS audio
(
    guid       uuid        NOT NULL,
    upload_uid text REFERENCES audio_upload (uid),
    track_id   text        NOT NULL,
    codec      varchar(20) NOT NULL,
    container  varchar(10) NOT NULL,
    bitrate    varchar(6)  NOT NULL
);
