/* (C)2024 */
package net.joostvdg.wwi.media;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record Movie(
    int id,
    String title,
    String platform,
    String director,
    int durationInMinutes,
    int releaseYear,
    Set<String> genre,
    Optional<String> url, // URL for the series, optional
    Optional<Map<String, String>>
        tags // Optional tags (e.g., {"Director": "John Doe", "Country": "USA"})
    ) implements Media {

  public static final String TYPE = "movie";

  @Override
  public int getId() {
    return id;
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
    // Convert the map of tags to a comma-separated string
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
    if (!(o instanceof Movie movie)) return false;

    return id == movie.id;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }
}
