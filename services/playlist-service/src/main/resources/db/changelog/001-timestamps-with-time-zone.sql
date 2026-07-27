SET search_path TO rcrs_playlist;

-- Timestamps are instants: store them zone-aware so they map to java.time.Instant
-- without an implicit local-zone interpretation. Existing values were written as UTC.
ALTER TABLE playlists
    ALTER COLUMN created_at TYPE timestamp with time zone USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamp with time zone USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE playlist_tracks
    ALTER COLUMN added_at TYPE timestamp with time zone USING added_at AT TIME ZONE 'UTC';
