package net.joostvdg.wwi.media;

import java.util.Objects;

public sealed interface Media permits Movie, Series, VideoGame {
    String getType();
    String getTitle();
    String getPlatform(); // e.g., Netflix, HBO, Steam, etc.
    String getGenres();
    String getTags();
    long getId();

}