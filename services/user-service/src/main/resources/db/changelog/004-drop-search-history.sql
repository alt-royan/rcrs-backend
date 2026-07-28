SET search_path TO rcrs_user;

-- Dropped in favour of library-service, which owns search history.
--
-- This table was created by 003-add-likes.sql and never used: no entity,
-- repository, service or endpoint ever referenced it. Its shape could not have
-- worked either — user_id was the primary key, so it could hold exactly one row
-- per user, and it had no timestamp to order by.
DROP TABLE IF EXISTS search_history;
