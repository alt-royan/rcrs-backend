--liquibase formatted sql

--changeset rcrs:004-add-user-avatar
CREATE TABLE rcrs_user.user_avatar
(
    user_id    text PRIMARY KEY,
    avatar_key text NOT NULL,
    CONSTRAINT fk_user_avatar_user FOREIGN KEY (user_id) REFERENCES rcrs_user.users (user_id)
);
