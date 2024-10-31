package net.joostvdg.wwi.media;

import java.util.Map;

public record SeriesProgress(
        long id,
        boolean finished, // Indicates if the series is finished
        Series series, // The series being tracked
        Map<String, Integer> progress, // Progress map (e.g., "Season 1" -> 5, meaning 5 episodes watched)
        boolean favorite // Indicates if the series is a favorite
) implements Progress {

    // Validation in the constructor
    public SeriesProgress {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        if (progress == null) {
            throw new IllegalArgumentException("Progress cannot be null");
        }
        if (progress.isEmpty()) {
            throw new IllegalArgumentException("Progress cannot be empty");
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
    public Series getMedia() {
        return series;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeriesProgress that = (SeriesProgress) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
