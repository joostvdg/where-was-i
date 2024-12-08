
CREATE TABLE movies (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    platform VARCHAR(255) NOT NULL,
    director VARCHAR(255) NOT NULL,
    duration_in_minutes INT NOT NULL,
    release_year INT NOT NULL,
    genre TEXT[] NOT NULL,
    url VARCHAR(255),
    tags JSONB
);


-- long id,
--         String title,
--         String platform,
--         String director,
--         int durationInMinutes,
--         int releaseYear,
-- Set<String> genre,
--     Optional<String> url, // URL for the series, optional
--     Optional<Map<String, String>> tags // Optional tags (e.g., {"Director": "John Doe", "Country": "USA"})