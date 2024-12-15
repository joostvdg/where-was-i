CREATE TABLE video_games (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    platform VARCHAR(255) NOT NULL,
    genre TEXT[] NOT NULL,
    publisher VARCHAR(255),
    developer VARCHAR(255),
    year INT NOT NULL CHECK (year BETWEEN 1970 AND 2030),
    tags JSONB
);