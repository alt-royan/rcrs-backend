SET search_path TO rcrs_media;

ALTER TABLE audio_upload ADD COLUMN created_at timestamp NOT NULL DEFAULT now();
