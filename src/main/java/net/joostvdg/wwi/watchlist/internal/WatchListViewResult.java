/* (C)2024 */
package net.joostvdg.wwi.watchlist.internal;

import java.util.ArrayList;
import java.util.List;
import net.joostvdg.wwi.model.wwi_watchlist.tables.records.WatchListWithMediaRecord;
import net.joostvdg.wwi.model.wwi_watchlist.tables.records.WatchListWithUsersRecord;

class WatchListViewResult {
  long id;
  List<WatchListWithUsersRecord> watchlistUserViewRecords;
  List<WatchListWithMediaRecord> watchListMediaViewRecords;

  public WatchListViewResult(long id) {
    this.id = id;
    this.watchListMediaViewRecords = new ArrayList<>();
    this.watchlistUserViewRecords = new ArrayList<>();
  }

  public WatchListViewResult(
      long id,
      List<WatchListWithUsersRecord> watchlistUserViewRecords,
      List<WatchListWithMediaRecord> watchListMediaViewRecords) {
    this.id = id;
    this.watchlistUserViewRecords = watchlistUserViewRecords;
    this.watchListMediaViewRecords = watchListMediaViewRecords;
  }

  public long getId() {
    return id;
  }

  public List<WatchListWithUsersRecord> getWatchlistUserViewRecords() {
    return watchlistUserViewRecords;
  }

  public void setWatchlistUserViewRecord(List<WatchListWithUsersRecord> watchlistUserViewRecords) {
    this.watchlistUserViewRecords = watchlistUserViewRecords;
  }

  public List<WatchListWithMediaRecord> getWatchListMediaViewRecords() {
    return watchListMediaViewRecords;
  }

  public void setWatchListMediaViewRecords(
      List<WatchListWithMediaRecord> watchListMediaViewRecords) {
    this.watchListMediaViewRecords = watchListMediaViewRecords;
  }

  public void addWatchListMediaViewRecord(WatchListWithMediaRecord watchListWithMediaRecord) {
    this.watchListMediaViewRecords.add(watchListWithMediaRecord);
  }

  public void addWatchlistUserViewRecord(WatchListWithUsersRecord watchListWithUsersRecord) {
    this.watchlistUserViewRecords.add(watchListWithUsersRecord);
  }
}
