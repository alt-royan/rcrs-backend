--liquibase formatted sql

--changeset rcrs:004-add-user-avatar
CREATE TABLE IF NOT EXISTS rcrs_user.user_avatar
(
    user_id    uuid PRIMARY KEY,
    avatar_key text NOT NULL,
    CONSTRAINT fk_user_avatar_user FOREIGN KEY (user_id) REFERENCES rcrs_user.users (id)
);
