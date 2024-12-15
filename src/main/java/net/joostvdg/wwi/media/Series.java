package net.joostvdg.wwi.media;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record Series(
        int id,
        String title,
        Set<String> genre,
        Map<String, Integer> seasons, // Map of season name (e.g., "Season 1") to the number of episodes
        String platform, // e.g., Netflix, HBO
        Optional<String> url, // URL for the series, optional
        Optional<LocalDate> releaseYear, // Optional release year
        Optional<LocalDate> endYear, // Optional end year
        Optional<Map<String, String>> tags // Optional tags (e.g., {"Director": "John Doe", "Country": "USA"})
) implements Media {
    public Series {
        // Validation logic
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty.");
        }
        if (genre == null || genre.isEmpty()) {
            throw new IllegalArgumentException("Genre cannot be null or empty.");
        }
        if (seasons == null || seasons.isEmpty()) {
            throw new IllegalArgumentException("Seasons cannot be null or empty.");
        }
        if (platform == null || platform.isEmpty()) {
            throw new IllegalArgumentException("Platform cannot be null or empty.");
        }
    }

    @Override
    public int getId() {
        return id;
    }
    @Override
    public String getType() {
        return "Series";
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    @Override
    public String getGenres() {
        // Convert the set of genres to a comma-separated string
        return String.join(", ", genre);
    }



    @Override
    public String getTags() {
        // Convert the map of tags to a comma-separated string
        return tags.map(tagMap -> tagMap.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce("", (s1, s2) -> s1 + ", " + s2))
                .orElse("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Series series = (Series) o;
        return id == series.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
