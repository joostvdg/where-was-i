-- Create the wwi_media schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS wwi_media;

-- Create the movies table in the wwi_media schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_media.movies (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    platform VARCHAR(255) NOT NULL,
    director VARCHAR(255) NOT NULL,
    duration_in_minutes INT NOT NULL,
    release_year INT NOT NULL,
    genre TEXT[] NOT NULL,
    url VARCHAR(255),
    tags JSONB,
    CONSTRAINT unique_movie_title_platform_release_year UNIQUE (title, platform, release_year)
);

-- Create indexes for movies table
CREATE INDEX IF NOT EXISTS idx_movies_id ON wwi_media.movies(id);
CREATE INDEX IF NOT EXISTS idx_movies_title ON wwi_media.movies(title);

-- Create the series table in the wwi_media schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_media.series (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre TEXT[] NOT NULL,
    seasons JSONB NOT NULL,
    platform VARCHAR(255) NOT NULL,
    url VARCHAR(255),
    release_year INT NOT NULL,
    end_year INT,
    tags JSONB,
    CONSTRAINT unique_series_title_platform_release_year UNIQUE (title, platform, release_year)
);

-- Create indexes for series table
CREATE INDEX IF NOT EXISTS idx_series_id ON wwi_media.series(id);
CREATE INDEX IF NOT EXISTS idx_series_title ON wwi_media.series(title);

-- Create the video_games table in the wwi_media schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_media.video_games (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    platform VARCHAR(255) NOT NULL,
    genre TEXT[] NOT NULL,
    publisher VARCHAR(255),
    developer VARCHAR(255),
    year INT NOT NULL CHECK (year BETWEEN 1970 AND 2030),
    tags JSONB,
    CONSTRAINT unique_video_game_title_platform_year UNIQUE (title, platform, year)
);

-- Create indexes for video_games table
CREATE INDEX IF NOT EXISTS idx_video_games_id ON wwi_media.video_games(id);
CREATE INDEX IF NOT EXISTS idx_video_games_title ON wwi_media.video_games(title);