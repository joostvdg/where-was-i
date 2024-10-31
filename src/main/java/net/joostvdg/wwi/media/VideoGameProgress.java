package net.joostvdg.wwi.media;

import java.util.Map;

public record VideoGameProgress(
        long id,
        Map<String, Integer> progress, // Map of progress for each section of the game
        boolean finished, // Indicates if the series is finished
        VideoGame videoGame, // The series being tracked
        boolean favorite // Indicates if the series is a favorite
) implements Progress {

    // Validation in the constructor
    public VideoGameProgress {
        if (videoGame == null) {
            throw new IllegalArgumentException("VideoGame cannot be null");
        }

        // create a map of progress for each section of the game
        // sections being "main" ,"side", "collectibles", "achievements"
        if (progress == null || progress.isEmpty()) {
            progress = Map.of(
                    "main", 0,
                    "side", 0,
                    "collectibles", 0,
                    "achievements", 0
            );
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Map<String, Integer> getProgress() {
        return progress;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public Media getMedia() {
        return videoGame;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoGameProgress that = (VideoGameProgress) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
