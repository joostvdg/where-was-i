CREATE TABLE series (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre TEXT[] NOT NULL,
    seasons JSONB NOT NULL,
    platform VARCHAR(255) NOT NULL,
    url VARCHAR(255),
    release_year INT NOT NULL,
    end_year INT,
    tags JSONB
);