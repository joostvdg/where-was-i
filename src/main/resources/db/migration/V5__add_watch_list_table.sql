CREATE TABLE watch_list (
        id SERIAL PRIMARY KEY,
        owner_id BIGINT NOT NULL,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        created TIMESTAMP NOT NULL,
        last_edit TIMESTAMP NOT NULL,
        favorite BOOLEAN NOT NULL,
        CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE watch_list_read_shared (
        watch_list_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        PRIMARY KEY (watch_list_id, user_id),
        CONSTRAINT fk_watch_list_read_shared_watch_list FOREIGN KEY (watch_list_id) REFERENCES watch_list(id),
        CONSTRAINT fk_watch_list_read_shared_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE watch_list_write_shared (
        watch_list_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        PRIMARY KEY (watch_list_id, user_id),
        CONSTRAINT fk_watch_list_write_shared_watch_list FOREIGN KEY (watch_list_id) REFERENCES watch_list(id),
        CONSTRAINT fk_watch_list_write_shared_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE watch_list_movies (
        watch_list_id BIGINT NOT NULL,
        movie_id BIGINT NOT NULL,
        PRIMARY KEY (watch_list_id, movie_id),
        CONSTRAINT fk_watch_list_movies_watch_list FOREIGN KEY (watch_list_id) REFERENCES watch_list(id),
        CONSTRAINT fk_watch_list_movies_movie FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE watch_list_series (
        watch_list_id BIGINT NOT NULL,
        series_id BIGINT NOT NULL,
        PRIMARY KEY (watch_list_id, series_id),
        CONSTRAINT fk_watch_list_series_watch_list FOREIGN KEY (watch_list_id) REFERENCES watch_list(id),
        CONSTRAINT fk_watch_list_series_series FOREIGN KEY (series_id) REFERENCES series(id)
);

CREATE TABLE watch_list_video_games (
        watch_list_id BIGINT NOT NULL,
        video_game_id BIGINT NOT NULL,
        PRIMARY KEY (watch_list_id, video_game_id),
        CONSTRAINT fk_watch_list_video_games_watch_list FOREIGN KEY (watch_list_id) REFERENCES watch_list(id),
        CONSTRAINT fk_watch_list_video_games_video_game FOREIGN KEY (video_game_id) REFERENCES video_games(id)
);

-- String accountNumber,
-- String accountType,
-- String username,
-- String name,
-- String email,
-- LocalDate dateJoined,
-- LocalDate dateLastLogin,


CREATE VIEW watch_list_with_users AS
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
    FROM watch_list wl
        JOIN users u_owner ON wl.owner_id = u_owner.id
            LEFT JOIN watch_list_read_shared wlrs ON wl.id = wlrs.watch_list_id
            LEFT JOIN users u_read ON wlrs.user_id = u_read.id
            LEFT JOIN watch_list_write_shared wlws ON wl.id = wlws.watch_list_id
            LEFT JOIN users u_write ON wlws.user_id = u_write.id;

-- Movie Specific Fields
-- director VARCHAR(255) NOT NULL,
-- duration_in_minutes INT NOT NULL,

-- Series Specific Fields
-- seasons JSONB NOT NULL,
-- release_year INT NOT NULL,
-- end_year INT,

-- Video Game Specific Fields
-- publisher VARCHAR(255),
-- developer VARCHAR(255),
-- year INT NOT NULL CHECK (year BETWEEN 1970 AND 2030),

CREATE VIEW watch_list_with_media AS
    SELECT wl.id AS watch_list_id,
        'movie' AS media_type,
        m.id AS media_id,
        m.title AS media_title,
        m.genre AS genre,
        NULL::JSONB AS seasons,
        NULL::INTEGER AS release_year,
        NULL::INTEGER AS end_year,
        m.director AS movie_director,
        m.duration_in_minutes AS movie_duration_in_minutes,
        NULL::VARCHAR AS publisher,
        NULL::VARCHAR AS developer,
        NULL::INTEGER AS year
    FROM watch_list wl
        JOIN watch_list_movies wlm ON wl.id = wlm.watch_list_id
        JOIN movies m ON wlm.movie_id = m.id

    UNION ALL

    SELECT wl.id AS watch_list_id,
        'series' AS media_type,
        s.id AS media_id,
        s.title AS media_title,
        s.genre AS genre,
        s.seasons,
        s.release_year,
        s.end_year,
        NULL::VARCHAR AS movie_director,
        NULL::INTEGER AS movie_duration_in_minutes,
        NULL::VARCHAR AS publisher,
        NULL::VARCHAR AS developer,
        NULL::INTEGER AS year
    FROM watch_list wl
        JOIN watch_list_series wls ON wl.id = wls.watch_list_id
        JOIN series s ON wls.series_id = s.id

    UNION ALL

    SELECT wl.id AS watch_list_id,
        'video_game' AS media_type,
        vg.id AS media_id,
        vg.title AS media_title,
        vg.genre AS genre,
        NULL::JSONB AS seasons,
        NULL::INTEGER AS release_year,
        NULL::INTEGER AS end_year,
        NULL::VARCHAR AS movie_director,
        NULL::INTEGER AS movie_duration_in_minutes,
        vg.publisher,
        vg.developer,
        vg.year
    FROM watch_list wl
        JOIN watch_list_video_games wlv ON wl.id = wlv.watch_list_id
        JOIN video_games vg ON wlv.video_game_id = vg.id;