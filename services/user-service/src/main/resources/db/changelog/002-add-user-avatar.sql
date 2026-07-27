SET search_path TO rcrs_user;

CREATE TABLE user_avatar
(
    user_id    text PRIMARY KEY,
    avatar_key text NOT NULL,
    CONSTRAINT fk_user_avatar_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);
