-- Create the wwi_watchlist schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS wwi_watchlist;

-- Create the watch_list table in the wwi_watchlist schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_watchlist.watch_list (
    id SERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created TIMESTAMP NOT NULL,
    last_edit TIMESTAMP NOT NULL,
    favorite BOOLEAN NOT NULL,
    CONSTRAINT unique_owner_id_name UNIQUE (owner_id, name),
    CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES wwi_auth.users(id)
);

-- Create indexes for watch_list table
CREATE INDEX IF NOT EXISTS idx_watch_list_id ON wwi_watchlist.watch_list(id);

-- Create the watch_list_read_shared table in the wwi_watchlist schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_watchlist.watch_list_read_shared (
    watch_list_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (watch_list_id, user_id),
    CONSTRAINT fk_watch_list_read_shared_watch_list FOREIGN KEY (watch_list_id) REFERENCES wwi_watchlist.watch_list(id),
    CONSTRAINT fk_watch_list_read_shared_user FOREIGN KEY (user_id) REFERENCES wwi_auth.users(id)
);

-- Create the watch_list_write_shared table in the wwi_watchlist schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_watchlist.watch_list_write_shared (
    watch_list_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (watch_list_id, user_id),
    CONSTRAINT fk_watch_list_write_shared_watch_list FOREIGN KEY (watch_list_id) REFERENCES wwi_watchlist.watch_list(id),
    CONSTRAINT fk_watch_list_write_shared_user FOREIGN KEY (user_id) REFERENCES wwi_auth.users(id)
);

-- Create the watch_list_movies table in the wwi_watchlist schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_watchlist.watch_list_movies (
    watch_list_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    PRIMARY KEY (watch_list_id, movie_id),
    CONSTRAINT fk_watch_list_movies_watch_list FOREIGN KEY (watch_list_id) REFERENCES wwi_watchlist.watch_list(id),
    CONSTRAINT fk_watch_list_movies_movie FOREIGN KEY (movie_id) REFERENCES wwi_media.movies(id)
);

-- Create the watch_list_series table in the wwi_watchlist schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_watchlist.watch_list_series (
    watch_list_id BIGINT NOT NULL,
    series_id BIGINT NOT NULL,
    PRIMARY KEY (watch_list_id, series_id),
    CONSTRAINT fk_watch_list_series_watch_list FOREIGN KEY (watch_list_id) REFERENCES wwi_watchlist.watch_list(id),
    CONSTRAINT fk_watch_list_series_series FOREIGN KEY (series_id) REFERENCES wwi_media.series(id)
);

-- Create the watch_list_video_games table in the wwi_watchlist schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_watchlist.watch_list_video_games (
    watch_list_id BIGINT NOT NULL,
    video_game_id BIGINT NOT NULL,
    PRIMARY KEY (watch_list_id, video_game_id),
    CONSTRAINT fk_watch_list_video_games_watch_list FOREIGN KEY (watch_list_id) REFERENCES wwi_watchlist.watch_list(id),
    CONSTRAINT fk_watch_list_video_games_video_game FOREIGN KEY (video_game_id) REFERENCES wwi_media.video_games(id)
);

-- Create a view to join watch_list with users
CREATE VIEW wwi_watchlist.watch_list_with_users AS
    SELECT wl.id AS watch_list_id,
        wl.owner_id,
        wl.name,
        wl.description,
        wl.created,
        wl.last_edit,
        wl.favorite,
        u_owner.id AS owner_user_id,
        u_owner.name AS owner_name,
        u_owner.email AS owner_email,
        u_owner.account_number AS owner_account_number,
        u_owner.account_type AS owner_account_type,
        u_owner.date_joined AS owner_date_joined,
        u_owner.date_last_login AS owner_date_last_login,
        u_read.id AS read_user_id,
        u_read.name AS read_user_name,
        u_read.email AS read_user_email,
        u_write.id AS write_user_id,
        u_write.name AS write_user_name,
        u_write.email AS write_user_email
    FROM wwi_watchlist.watch_list wl
        JOIN wwi_auth.users u_owner ON wl.owner_id = u_owner.id
        LEFT JOIN wwi_watchlist.watch_list_read_shared wlrs ON wl.id = wlrs.watch_list_id
        LEFT JOIN wwi_auth.users u_read ON wlrs.user_id = u_read.id
        LEFT JOIN wwi_watchlist.watch_list_write_shared wlws ON wl.id = wlws.watch_list_id
        LEFT JOIN wwi_auth.users u_write ON wlws.user_id = u_write.id;

-- Create a view to join watch_list with media
CREATE VIEW wwi_watchlist.watch_list_with_media AS
    SELECT wl.id AS watch_list_id,
        'movie' AS media_type,
        m.id AS media_id,
        m.title AS media_title,
        m.platform,
        m.genre AS genre,
        NULL::JSONB AS seasons,
        m.release_year,
        NULL::INTEGER AS end_year,
        m.director AS movie_director,
        m.duration_in_minutes AS movie_duration_in_minutes,
        m.url,
        NULL::VARCHAR AS publisher,
        NULL::VARCHAR AS developer,
        NULL::INTEGER AS year,
        tags
    FROM wwi_watchlist.watch_list wl
        JOIN wwi_watchlist.watch_list_movies wlm ON wl.id = wlm.watch_list_id
        JOIN wwi_media.movies m ON wlm.movie_id = m.id

    UNION ALL

    SELECT wl.id AS watch_list_id,
        'series' AS media_type,
        s.id AS media_id,
        s.title AS media_title,
        s.platform,
        s.genre AS genre,
        s.seasons,
        s.release_year,
        s.end_year,
        NULL::VARCHAR AS movie_director,
        NULL::INTEGER AS movie_duration_in_minutes,
        NULL::VARCHAR AS url,
        NULL::VARCHAR AS publisher,
        NULL::VARCHAR AS developer,
        NULL::INTEGER AS year,
        tags
    FROM wwi_watchlist.watch_list wl
        JOIN wwi_watchlist.watch_list_series wls ON wl.id = wls.watch_list_id
        JOIN wwi_media.series s ON wls.series_id = s.id

    UNION ALL

    SELECT wl.id AS watch_list_id,
        'videogame' AS media_type,
        vg.id AS media_id,
        vg.title AS media_title,
        vg.platform,
        vg.genre AS genre,
        NULL::JSONB AS seasons,
        NULL::INTEGER AS release_year,
        NULL::INTEGER AS end_year,
        NULL::VARCHAR AS movie_director,
        NULL::INTEGER AS movie_duration_in_minutes,
        NULL::VARCHAR AS url,
        vg.publisher,
        vg.developer,
        vg.year,
        tags
    FROM wwi_watchlist.watch_list wl
        JOIN wwi_watchlist.watch_list_video_games wlv ON wl.id = wlv.watch_list_id
        JOIN wwi_media.video_games vg ON wlv.video_game_id = vg.id;