SET search_path TO rcrs_catalog;

CREATE TABLE IF NOT EXISTS artists
(
    id            uuid PRIMARY KEY,
    name          text NOT NULL,
    avatar_s3_key text,
    social_links json
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
    track_id uuid,
    name     text,
    roles    varchar(20)[],
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


CREATE OR REPLACE VIEW artist_view AS
SELECT a.*
FROM artists a;

CREATE OR REPLACE VIEW other_artist_view AS
SELECT a.*
FROM other_artist a;

CREATE OR REPLACE VIEW artists_on_album_view AS
SELECT ata.album_id, a.id, a.name, a.avatar_s3_key, ata.artist_role AS role
FROM artist_to_album ata
         LEFT JOIN artists a ON a.id = ata.artist_id;


CREATE OR REPLACE VIEW artists_on_track_view AS
SELECT att.track_id, a.id, a.name, a.avatar_s3_key, att.artist_role AS role
FROM artist_to_track att
         LEFT JOIN artists a ON a.id = att.artist_id;


CREATE OR REPLACE VIEW album_view AS
SELECT a.id,
       a.status,
       a.title,
       a.type,
       a.release_date,
       date_part('year', a.release_date)                                           AS year,
       (SELECT count(*) from tracks WHERE tracks.album_id = a.id)                  AS total_tracks,
       (SELECT sum(tracks.duration_ms) from tracks WHERE tracks.album_id = a.id)   AS total_duration_ms,
       a.cover_s3_key,
       (SELECT bool_and(tracks.explicit) from tracks WHERE tracks.album_id = a.id) AS explicit,
       a.available,
       json_agg(aoa)                                                               AS artists
FROM albums a
         LEFT JOIN artists_on_album_view aoa ON a.id = aoa.album_id
GROUP BY a.id;

CREATE OR REPLACE VIEW track_view AS
SELECT t.id,
       t.status,
       t.title,
       t.release_date,
       t.duration_ms,
       t.track_number,
       t.explicit,
       t.available,
       t.album_id,
       json_agg(aot) AS artists
FROM tracks t
         LEFT JOIN artists_on_track_view aot ON t.id = aot.track_id
GROUP BY t.id;

CREATE OR REPLACE VIEW track_with_album_view AS
SELECT t.id,
       t.status,
       t.title,
       t.release_date,
       t.duration_ms,
       t.track_number,
       t.explicit,
       t.available,
       t.artists,
       row_to_json(a) AS album
FROM track_view t
         LEFT JOIN album_view a ON t.album_id = a.id;

