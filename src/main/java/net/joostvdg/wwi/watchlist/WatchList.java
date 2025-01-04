/* (C)2024 */
package net.joostvdg.wwi.watchlist;

import java.time.Instant;
import java.util.Set;
import net.joostvdg.wwi.media.Media;
import net.joostvdg.wwi.user.User;

public class WatchList {
  private final int id;
  private final Set<Media> items; // A set of media items (Movie, Series, VideoGame)
  private final User owner;
  private final Set<User> readShared; // Users who can read this list
  private final Set<User> writeShared; // Users who can modify this list
  private String name;
  private String description;
  private Instant created;
  private Instant lastEdit;
  private boolean favorite;

  public WatchList(
      int id,
      Set<Media> items,
      User owner,
      Set<User> readShared,
      Set<User> writeShared,
      String name,
      String description,
      Instant created,
      Instant lastEdit,
      boolean favorite) {
    this.id = id;
    this.items = items;
    this.owner = owner;
    this.readShared = readShared;
    this.writeShared = writeShared;
    this.name = name;
    this.description = description;
    this.created = created;
    this.lastEdit = lastEdit;
    this.favorite = favorite;
  }

  public int getId() {
    return id;
  }

  public Set<Media> getItems() {
    return items;
  }

  public User getOwner() {
    return owner;
  }

  public Set<User> getReadShared() {
    return readShared;
  }

  public Set<User> getWriteShared() {
    return writeShared;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    this.lastEdit = Instant.now(); // Update last edit timestamp
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
    this.lastEdit = Instant.now(); // Update last edit timestamp
  }

  public Instant getCreated() {
    return created;
  }

  public Instant getLastEdit() {
    return lastEdit;
  }

  public boolean isFavorite() {
    return favorite;
  }

  public void setFavorite(boolean favorite) {
    this.favorite = favorite;
  }

  public void addItem(Media newMediaItem) {
    this.items.add(newMediaItem);
    this.lastEdit = Instant.now(); // Update last edit timestamp
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof WatchList watchList)) return false;

    return id == watchList.id;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
