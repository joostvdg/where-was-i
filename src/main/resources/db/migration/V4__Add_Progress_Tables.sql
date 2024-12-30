-- Create the wwi_progress schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS wwi_progress;

-- Create the series_progress table in the wwi_progress schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_progress.series_progress (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES wwi_auth.users(id),
    series_id INTEGER NOT NULL REFERENCES wwi_media.series(id),
    finished BOOLEAN NOT NULL,
    progress JSONB NOT NULL,
    favorite BOOLEAN NOT NULL,
    CONSTRAINT fk_series_progress_user FOREIGN KEY (user_id) REFERENCES wwi_auth.users(id),
    CONSTRAINT fk_series_progress_series FOREIGN KEY (series_id) REFERENCES wwi_media.series(id)
);

-- Create indexes for series_progress table
CREATE INDEX IF NOT EXISTS idx_series_progress_id ON wwi_progress.series_progress(id);
CREATE INDEX IF NOT EXISTS idx_series_progress_user_id ON wwi_progress.series_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_series_progress_series_id ON wwi_progress.series_progress(series_id);

-- Create the video_game_progress table in the wwi_progress schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_progress.video_game_progress (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES wwi_auth.users(id),
    video_game_id INTEGER NOT NULL REFERENCES wwi_media.video_games(id),
    progress JSONB NOT NULL,
    finished BOOLEAN NOT NULL,
    favorite BOOLEAN NOT NULL,
    CONSTRAINT fk_video_game_progress_user FOREIGN KEY (user_id) REFERENCES wwi_auth.users(id),
    CONSTRAINT fk_video_game_progress_video_game FOREIGN KEY (video_game_id) REFERENCES wwi_media.video_games(id)
);

-- Create indexes for video_game_progress table
CREATE INDEX IF NOT EXISTS idx_video_game_progress_id ON wwi_progress.video_game_progress(id);
CREATE INDEX IF NOT EXISTS idx_video_game_progress_user_id ON wwi_progress.video_game_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_video_game_progress_video_game_id ON wwi_progress.video_game_progress(video_game_id);

-- Create the movie_progress table in the wwi_progress schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_progress.movie_progress (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES wwi_auth.users(id),
    movie_id INTEGER NOT NULL REFERENCES wwi_media.movies(id),
    progress JSONB NOT NULL,
    favorite BOOLEAN NOT NULL,
    finished BOOLEAN NOT NULL,
    CONSTRAINT fk_movie_progress_user FOREIGN KEY (user_id) REFERENCES wwi_auth.users(id),
    CONSTRAINT fk_movie_progress_movie FOREIGN KEY (movie_id) REFERENCES wwi_media.movies(id)
);

-- Create indexes for movie_progress table
CREATE INDEX IF NOT EXISTS idx_movie_progress_id ON wwi_progress.movie_progress(id);
CREATE INDEX IF NOT EXISTS idx_movie_progress_user_id ON wwi_progress.movie_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_movie_progress_movie_id ON wwi_progress.movie_progress(movie_id);

-- Create a view to join all progress tables
CREATE VIEW wwi_progress.all_progress AS
    SELECT sp.id AS progress_id,
        sp.user_id,
        sp.finished,
        sp.progress,
        sp.favorite,
        'series' AS media_type,
        sp.series_id AS media_id,
        s.title AS media_title,
        s.platform AS media_platform,
        s.genre AS media_genre,
        s.release_year AS media_release_year,
        s.tags AS media_tags,
        s.seasons AS media_seasons,
        s.url AS media_url,
        s.end_year AS media_end_year,
        NULL::VARCHAR as media_director,
        NULL::INTEGER as media_duration_in_minutes,
        NULL::VARCHAR as media_publisher,
        NULL::VARCHAR as media_developer
    FROM wwi_progress.series_progress sp
        JOIN wwi_media.series s ON sp.series_id = s.id

    UNION ALL

    SELECT vgp.id AS progress_id,
        vgp.user_id,
        vgp.finished,
        vgp.progress,
        vgp.favorite,
        'videogame' AS media_type,
        vgp.video_game_id AS media_id,
        vg.title AS media_title,
        vg.platform AS media_platform,
        vg.genre AS media_genre,
        vg.year AS media_release_year,
        vg.tags AS media_tags,
        NULL::JSONB as media_seasons,
        NULL::VARCHAR as media_url,
        NULL::INTEGER as media_end_year,
        NULL::VARCHAR as media_director,
        NULL::INTEGER as media_duration_in_minutes,
        vg.publisher AS media_publisher,
        vg.developer AS media_developer
    FROM wwi_progress.video_game_progress vgp
        JOIN wwi_media.video_games vg ON vgp.video_game_id = vg.id

    UNION ALL

    SELECT mp.id AS progress_id,
        mp.user_id,
        mp.finished,
        mp.progress,
        mp.favorite,
        'movie' AS media_type,
        mp.movie_id AS media_id,
        m.title AS media_title,
        m.platform AS media_platform,
        m.genre AS media_genre,
        m.release_year AS media_release_year,
        m.tags AS media_tags,
        NULL::JSONB as media_seasons,
        m.url as media_url,
        NULL::INTEGER as media_end_year,
        m.director AS media_director,
        m.duration_in_minutes AS media_duration_in_minutes,
        NULL::VARCHAR as media_publisher,
        NULL::VARCHAR as media_developer
    FROM wwi_progress.movie_progress mp
        JOIN wwi_media.movies m ON mp.movie_id = m.id;