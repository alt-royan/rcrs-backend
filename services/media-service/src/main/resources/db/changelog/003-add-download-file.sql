SET search_path TO rcrs_media;

CREATE TABLE IF NOT EXISTS download_file
(
    id                 uuid PRIMARY KEY REFERENCES audio (id) ON DELETE CASCADE,
    guid               uuid                     NOT NULL,
    key                text                     NOT NULL,
    file_name          text                     NOT NULL,
    content_type       text                     NOT NULL,
    creation_timestamp timestamp with time zone NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS download_file_guid_uidx ON download_file (guid);
