package net.joostvdg.wwi.media;

import java.util.Map;

public record MovieProgress(long id, Movie movie, Map<String, Integer> progress, boolean favorite, boolean finished) implements Progress {

    @Override
    public Movie getMedia() {
        return movie;
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
    public boolean isFavorite() {
        return favorite;
    }

    // Example method to mark the movie as finished
    public boolean isFinished() {
        return finished;
    }
}
