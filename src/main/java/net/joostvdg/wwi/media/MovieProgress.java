/* (C)2024 */
package net.joostvdg.wwi.media;

import java.util.Map;

public record MovieProgress(
    int id, Movie movie, Map<String, Integer> progress, boolean favorite, boolean finished)
    implements Progress {

  @Override
  public Movie getMedia() {
    return movie;
  }

  @Override
  public int getId() {
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
  @Override
  public boolean isFinished() {
    return finished;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MovieProgress movieProgress)) return false;

    return id == movieProgress.id;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public String getSummary() {
    // traverse the progress map and create a summary
    StringBuilder summary = new StringBuilder();
    progress.forEach((key, value) -> summary.append(key).append(": ").append(value).append(", "));
    return summary.toString();
  }
}
