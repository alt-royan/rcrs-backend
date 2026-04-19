CREATE OR REPLACE VIEW artist_album_view AS
SELECT ata.artist_id,
       ata.artist_role,
       a.id AS album_id,
       a.status,
       a.title,
       a.type,
       a.release_date,
       a.year,
       a.total_tracks,
       a.total_duration_ms,
       a.cover_s3_key,
       a.explicit,
       a.available,
       a.artists
FROM artist_to_album ata
         LEFT JOIN album_view a ON a.id = ata.album_id;

CREATE OR REPLACE VIEW artist_track_view AS
SELECT att.artist_id,
       att.artist_role,
       t.id AS track_id,
       t.status,
       t.title,
       t.release_date,
       t.duration_ms,
       t.track_number,
       t.explicit,
       t.available,
       t.artists,
       t.album
FROM artist_to_track att
         LEFT JOIN track_view t ON t.id = att.track_id;