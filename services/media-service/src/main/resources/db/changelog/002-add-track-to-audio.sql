SET search_path TO rcrs_media;

CREATE TABLE IF NOT EXISTS track_to_audio
(
    track_id text    NOT NULL,
    guid     uuid    NOT NULL UNIQUE REFERENCES audio (guid),
    main     boolean NOT NULL
);
