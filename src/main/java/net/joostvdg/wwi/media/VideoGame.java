/* (C)2024 */
package net.joostvdg.wwi.media;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

// TODO: add release year, url fields
public record VideoGame(
    int id,
    String title,
    String platform,
    Set<String> genre,
    String publisher,
    String developer,
    int year,
    Optional<Map<String, String>> tags)
    implements Media {

  public static final String TYPE = "videogame";

  public VideoGame {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title cannot be null or empty");
    }
    if (platform == null || platform.isBlank()) {
      throw new IllegalArgumentException("Platform cannot be null or empty");
    }
    if (year < 1970 || year > 2030) {
      throw new IllegalArgumentException("Year must be between 1970 and 2030");
    }
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public String toString() {
    return title + " (" + year + ")";
  }

  @Override
  public String getType() {
    return TYPE;
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
    return String.join(", ", genre);
  }

  @Override
  public String getTags() {
    return tags.map(
            tagMap ->
                tagMap.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .reduce("", (s1, s2) -> s1 + ", " + s2))
        .orElse("");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VideoGame videoGame)) return false;

    return id == videoGame.id;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }
}
