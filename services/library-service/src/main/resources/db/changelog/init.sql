CREATE SCHEMA IF NOT EXISTS rcrs_library;

SET search_path TO rcrs_library;

-- ---------------------------------------------------------------- LIKES ----
-- One polymorphic table rather than four per-entity tables: every query is
-- scoped by (user_id, entity_type) so the composite index gives the same
-- physical clustering separate tables would, and the hot "which of these 50
-- items are liked?" check stays a single round trip instead of a 4-way union.
CREATE TABLE IF NOT EXISTS likes
(
    user_id     text                     NOT NULL,
    entity_type text                     NOT NULL, -- TRACK | ALBUM | ARTIST | PLAYLIST
    entity_id   text                     NOT NULL, -- Url62
    liked_at    timestamp with time zone NOT NULL,
    PRIMARY KEY (user_id, entity_type, entity_id)
);

CREATE INDEX IF NOT EXISTS ix_likes_user_type_time ON likes (user_id, entity_type, liked_at DESC);
CREATE INDEX IF NOT EXISTS ix_likes_entity ON likes (entity_type, entity_id);

-- ------------------------------------------------------------ DOWNLOADS ----
-- The unit that actually exists on the device is one track file at one
-- quality, so this is keyed without quality: re-downloading a track at a
-- different quality replaces the row rather than creating a second one.
--
-- explicit = the user downloaded this single track directly, as opposed to it
-- arriving as part of an album or playlist. Explicit tracks survive the
-- deletion of every collection that happens to contain them.
CREATE TABLE IF NOT EXISTS downloaded_tracks
(
    user_id  text                     NOT NULL,
    track_id text                     NOT NULL, -- Url62
    quality  text                     NOT NULL, -- LOW | MID | HIGH
    explicit boolean                  NOT NULL DEFAULT false,
    added_at timestamp with time zone NOT NULL,
    PRIMARY KEY (user_id, track_id)
);

CREATE INDEX IF NOT EXISTS ix_dl_tracks_user_time ON downloaded_tracks (user_id, added_at DESC);

-- What the user thinks they downloaded, so the Downloads screen can show
-- "Album X" as one card instead of 12 loose tracks.
CREATE TABLE IF NOT EXISTS downloaded_collections
(
    user_id     text                     NOT NULL,
    entity_type text                     NOT NULL, -- ALBUM | PLAYLIST
    entity_id   text                     NOT NULL, -- Url62
    quality     text                     NOT NULL,
    track_count int                      NOT NULL,
    added_at    timestamp with time zone NOT NULL,
    PRIMARY KEY (user_id, entity_type, entity_id)
);

CREATE INDEX IF NOT EXISTS ix_dl_coll_user_time ON downloaded_collections (user_id, added_at DESC);

-- Membership, so deleting one collection can tell which of its tracks are
-- still claimed by another collection and must stay on the device.
CREATE TABLE IF NOT EXISTS downloaded_collection_tracks
(
    user_id     text NOT NULL,
    entity_type text NOT NULL,
    entity_id   text NOT NULL,
    track_id    text NOT NULL,
    PRIMARY KEY (user_id, entity_type, entity_id, track_id),
    FOREIGN KEY (user_id, entity_type, entity_id)
        REFERENCES downloaded_collections (user_id, entity_type, entity_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_dl_coll_tracks_user_track ON downloaded_collection_tracks (user_id, track_id);

-- ------------------------------------------------------- SEARCH HISTORY ----
-- A row is written when the user TAPS a result, not when they type. query_raw
-- records the query that led to the tap and is nullable, because the user may
-- have reached the entity without searching.
--
-- The primary key makes a repeat tap an upsert: it bumps searched_at and moves
-- the entry to the top instead of accumulating duplicates.
CREATE TABLE IF NOT EXISTS search_history
(
    user_id     text                     NOT NULL,
    entity_type text                     NOT NULL, -- TRACK | ALBUM | ARTIST | PLAYLIST
    entity_id   text                     NOT NULL, -- Url62
    query_raw   text,
    searched_at timestamp with time zone NOT NULL,
    PRIMARY KEY (user_id, entity_type, entity_id)
);

CREATE INDEX IF NOT EXISTS ix_search_hist_user_time ON search_history (user_id, searched_at DESC);
