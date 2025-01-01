/* (C)2024 */
package net.joostvdg.wwi.media;

public sealed interface Media permits Movie, Series, VideoGame {
  String getType();

  String getTitle();

  String getPlatform(); // e.g., Netflix, HBO, Steam, etc.

  String getGenres();

  String getTags();

  int getId();
}
