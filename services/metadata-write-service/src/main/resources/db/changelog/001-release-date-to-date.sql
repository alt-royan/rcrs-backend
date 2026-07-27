SET search_path TO rcrs_catalog;

-- release_date is a calendar date, not an instant: drop the spurious time-of-day
-- and zone so it can no longer shift across time zones.
ALTER TABLE albums
    ALTER COLUMN release_date TYPE date USING (release_date AT TIME ZONE 'UTC')::date;
