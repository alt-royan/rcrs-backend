CREATE SCHEMA IF NOT EXISTS rcrs_playlist;

SET search_path TO rcrs_playlist;

CREATE TABLE IF NOT EXISTS playlists
(
    id           uuid PRIMARY KEY,
    owner_id     text                     NOT NULL,
    title        text                     NOT NULL,
    description  text,
    tags         text[],
    cover_s3_key text,
    is_private   boolean                  NOT NULL DEFAULT true,
    type         text                     NOT NULL,
    created_at   timestamp with time zone NOT NULL,
    updated_at   timestamp with time zone NOT NULL
);

CREATE TABLE IF NOT EXISTS playlist_tracks
(
    playlist_id uuid                     NOT NULL,
    track_id    text                     NOT NULL,
    position    int                      NOT NULL,
    added_at    timestamp with time zone NOT NULL,
    PRIMARY KEY (playlist_id, track_id),
    FOREIGN KEY (playlist_id) REFERENCES playlists (id) ON DELETE CASCADE
);
