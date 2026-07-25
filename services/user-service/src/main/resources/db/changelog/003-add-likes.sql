SET search_path TO rcrs_user;

CREATE TABLE search_history
(
    user_id     text PRIMARY KEY,
    entity_id   text NOT NULL,
    entity_type text NOT NULL,
    CONSTRAINT fk_user_avatar_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);


