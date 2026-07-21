--liquibase formatted sql

--changeset rcrs:002-add-user-enabled-email-verified
ALTER TABLE rcrs_user.users ADD COLUMN IF NOT EXISTS enabled boolean NOT NULL DEFAULT true;
ALTER TABLE rcrs_user.users ADD COLUMN IF NOT EXISTS email_verified boolean NOT NULL DEFAULT false;
