CREATE SCHEMA IF NOT EXISTS rcrs_playlist;

SET search_path TO rcrs_playlist;

CREATE TABLE IF NOT EXISTS playlists
(
    id           text PRIMARY KEY,
    owner_id     text    NOT NULL,
    title        text    NOT NULL,
    description  text,
    tags         text[],
    cover_s3_key text,
    is_private   boolean NOT NULL DEFAULT true,
    type         text    NOT NULL DEFAULT 'CUSTOM',
    track_count  int     NOT NULL DEFAULT 0,
    created_at   timestamp NOT NULL,
    updated_at   timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS playlist_tracks
(
    id          uuid PRIMARY KEY,
    playlist_id text NOT NULL,
    track_id    text NOT NULL,
    position    int  NOT NULL,
    added_at    timestamp NOT NULL,
    FOREIGN KEY (playlist_id) REFERENCES playlists (id)
);
