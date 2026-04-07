SET search_path TO rcrs_catalog;

CREATE TYPE social_link AS
(
    resource_name text,
    url           text
);

CREATE TYPE artist_other AS
(
    name        text,
    social_link social_link,
    roles       varchar(20)[]
);

CREATE TABLE IF NOT EXISTS artists
(
    id            uuid PRIMARY KEY,
    name          text NOT NULL,
    social_links  social_link[],
    avatar_s3_key text
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

CREATE TABLE IF NOT EXISTS others_on_track
(
    track_id uuid,
    others   artist_other[],
    UNIQUE (track_id),
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


CREATE OR REPLACE VIEW artists_on_album AS
SELECT ata.album_id,
       json_agg(json_object('id' : a.id,
                            'name' : a.name,
                            'avatar_s3_key' : a.avatar_s3_key,
                            'artist_role' : ata.artist_role)) AS artists
FROM artist_to_album ata
         LEFT JOIN artists a ON a.id = ata.artist_id
GROUP BY ata.album_id;


CREATE OR REPLACE VIEW artists_on_track AS
SELECT att.track_id,
       json_agg(json_object('id' : a.id,
                            'name' : a.name,
                            'avatar_s3_key' : a.avatar_s3_key,
                            'artist_role' : att.artist_role)) AS artists
FROM artist_to_track att
         LEFT JOIN artists a ON a.id = att.artist_id
GROUP BY att.track_id;


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
       aoa.artists
FROM albums a
         LEFT JOIN artists_on_album aoa ON a.id = aoa.album_id;

CREATE OR REPLACE VIEW track_view AS
SELECT t.id,
       t.status,
       t.title,
       t.release_date,
       t.duration_ms,
       t.track_number,
       t.explicit,
       t.available,
       json_object('id' : a.id,
                   'title' : a.title,
                   'cover_s3_key' : a.cover_s3_key) AS album,
       aot.artists
FROM tracks t
         LEFT JOIN artists_on_track aot ON t.id = aot.track_id
         LEFT JOIN albums a ON t.album_id = a.id;

CREATE OR REPLACE VIEW track_in_album_view AS
SELECT t.id,
       t.status,
       t.title,
       t.release_date,
       t.duration_ms,
       t.track_number,
       t.explicit,
       t.available,
       t.album_id,
       aot.artists
FROM tracks t
         LEFT JOIN artists_on_track aot ON t.id = aot.track_id;

