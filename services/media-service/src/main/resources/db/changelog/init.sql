SET search_path TO rcrs_upload;

CREATE TABLE IF NOT EXISTS audio_upload
(
    uid                text PRIMARY KEY,
    status             text NOT NULL,
    original_file_name text NOT NULL,
    content_length     bigint,
    expires_at         timestamp,
    error              text
);
