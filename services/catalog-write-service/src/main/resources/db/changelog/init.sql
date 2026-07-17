SET search_path TO rcrs_catalog;

CREATE TABLE IF NOT EXISTS artists
(
    id            uuid PRIMARY KEY,
    name          text NOT NULL,
    avatar_s3_key text,
    social_links  json,
    tags          VARCHAR(20)[]
);

CREATE TABLE IF NOT EXISTS albums
(
    id           uuid PRIMARY KEY,
    status       text NOT NULL,
    title        text NOT NULL,
    type         text NOT NULL,
    release_date timestamp with time zone,
    cover_s3_key text,
    available    boolean
);

CREATE TABLE IF NOT EXISTS artist_to_album
(
    artist_id   uuid,
    album_id    uuid,
    artist_role text NOT NULL,
    PRIMARY KEY (artist_id, album_id),
    FOREIGN KEY (artist_id) REFERENCES artists (id),
    FOREIGN KEY (album_id) REFERENCES albums (id)
);

CREATE TABLE IF NOT EXISTS tracks
(
    id           uuid PRIMARY KEY,
    status       text    NOT NULL,
    title        text    NOT NULL,
    release_date timestamp with time zone,
    duration_ms  int,
    track_number int     NOT NULL,
    explicit     boolean NOT NULL,
    available    boolean NOT NULL,
    album_id     uuid    NOT NULL,
    FOREIGN KEY (album_id) REFERENCES albums (id)
);

CREATE TABLE IF NOT EXISTS other_artist
(
    id           uuid PRIMARY KEY,
    track_id     uuid NOT NULL,
    name         text,
    roles        text,
    social_links json,
    FOREIGN KEY (track_id) REFERENCES tracks (id)
);

CREATE TABLE IF NOT EXISTS artist_to_track
(
    artist_id   uuid,
    track_id    uuid,
    artist_role text NOT NULL,
    PRIMARY KEY (artist_id, track_id),
    FOREIGN KEY (artist_id) REFERENCES artists (id),
    FOREIGN KEY (track_id) REFERENCES tracks (id)
);
