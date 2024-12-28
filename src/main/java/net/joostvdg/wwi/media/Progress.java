package net.joostvdg.wwi.media;

import java.util.Map;

public sealed interface Progress permits MovieProgress, SeriesProgress, VideoGameProgress {
    int getId(); // Unique identifier for the progress
    Map<String, Integer> getProgress();
    boolean isFinished();// Progress tracked as a map (e.g., for series: "Season 1" -> 5/10 episodes)
    boolean isFavorite(); // Indicator if this is marked as a favorite
    Media getMedia(); // Method to retrieve the media being tracked (Series, Movie, VideoGame, etc.)

    String getSummary(); // Method to generate a summary of the progress
}
