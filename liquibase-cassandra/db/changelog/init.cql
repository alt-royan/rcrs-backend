CREATE KEYSPACE IF NOT EXISTS rcrs_catalog WITH REPLICATION = {
    'class' : 'SimpleStrategy',
    'replication_factor' : 1
    };

CREATE TYPE IF NOT EXISTS rcrs_catalog.social_link
    (
        resource_name text,
        url           text
    );

CREATE TABLE IF NOT EXISTS rcrs_catalog.artists
(
    id            uuid,
    name          text,
    social_links  list<frozen<rcrs_catalog.social_link>>,
    avatar_s3_key text,
    PRIMARY KEY (id)
);

CREATE TYPE IF NOT EXISTS rcrs_catalog.artist_other
    (
        name        text,
        social_link frozen<rcrs_catalog.social_link>,
        roles       set<varchar>
    );

CREATE TYPE IF NOT EXISTS rcrs_catalog.artists_on
    (
        main_artists     set<uuid>,
        featured_artists set<uuid>
    );

CREATE TABLE IF NOT EXISTS rcrs_catalog.albums
(
    id                uuid,
    status            text,
    title             text,
    total_duration_ms int,
    type              text,
    year              smallint,
    release_date      timestamp,
    cover_s3_key      text,
    total_tracks      smallint,
    explicit          boolean,
    available         boolean,
    artists           frozen<rcrs_catalog.artists_on>,
    PRIMARY KEY ( id, status )
);

CREATE INDEX ON rcrs_catalog.albums (status) USING 'sai';

CREATE TABLE IF NOT EXISTS rcrs_catalog.albums_by_artist
(
    artist_id         uuid,
    album_status      text,
    artist_role       text,
    album_type        text,
    release_date      timestamp,
    album_id          uuid,
    title             text,
    total_duration_ms int,
    year              smallint,
    cover_s3_key      text,
    total_tracks      smallint,
    explicit          boolean,
    available         boolean,
    artists           frozen<rcrs_catalog.artists_on>,
    PRIMARY KEY ( artist_id, album_status, artist_role, album_type, release_date )
);

CREATE INDEX ON rcrs_catalog.albums_by_artist (album_status) USING 'sai';

CREATE TABLE IF NOT EXISTS rcrs_catalog.tracks
(
    id           uuid,
    status       text,
    title        text,
    release_date timestamp,
    duration_ms  int,
    track_number smallint,
    explicit     boolean,
    available    boolean,
    album_id     uuid,
    artists      frozen<rcrs_catalog.artists_on>,
    others       list<frozen<rcrs_catalog.artist_other>>,
    PRIMARY KEY ( id, status )
);

CREATE INDEX ON rcrs_catalog.tracks (status) USING 'sai';

CREATE TABLE IF NOT EXISTS rcrs_catalog.tracks_by_artist
(
    artist_id    uuid,
    track_status text,
    artist_role  text,
    release_date timestamp,
    track_id     uuid,
    title        text,
    duration_ms  int,
    track_number smallint,
    explicit     boolean,
    available    boolean,
    album_id     uuid,
    artists      frozen<rcrs_catalog.artists_on>,
    others       list<frozen<rcrs_catalog.artist_other>>,
    PRIMARY KEY ( artist_id, track_status, artist_role, release_date )
);

CREATE INDEX ON rcrs_catalog.tracks_by_artist (track_status) USING 'sai';

CREATE TABLE IF NOT EXISTS rcrs_catalog.tracks_by_album
(
    album_id     uuid,
    track_status text,
    track_number smallint,
    track_id     uuid,
    title        text,
    duration_ms  int,
    explicit     boolean,
    available    boolean,
    artists      frozen<rcrs_catalog.artists_on>,
    PRIMARY KEY ( album_id, track_status, track_number )
);

CREATE INDEX ON rcrs_catalog.tracks_by_album (track_status) USING 'sai';