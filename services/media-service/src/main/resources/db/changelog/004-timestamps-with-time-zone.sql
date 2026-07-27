SET search_path TO rcrs_media;

-- These columns back java.time.Instant fields, so store them zone-aware instead of
-- letting the value be reinterpreted in the session's local zone. Existing values were UTC.
ALTER TABLE audio_upload
    ALTER COLUMN expires_at TYPE timestamp with time zone USING expires_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE timestamp with time zone USING created_at AT TIME ZONE 'UTC';
