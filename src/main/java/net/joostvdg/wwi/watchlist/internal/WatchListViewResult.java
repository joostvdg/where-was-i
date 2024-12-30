package net.joostvdg.wwi.watchlist.internal;


import net.joostvdg.wwi.model.wwi_watchlist.tables.records.WatchListWithMediaRecord;
import net.joostvdg.wwi.model.wwi_watchlist.tables.records.WatchListWithUsersRecord;

import java.util.ArrayList;
import java.util.List;

class WatchListViewResult {
    long id;
    WatchListWithUsersRecord watchlistUserViewRecord;
    List<WatchListWithMediaRecord> watchListMediaViewRecords;

    public WatchListViewResult(long id) {
        this.id = id;
        this.watchListMediaViewRecords = new ArrayList<>();
    }

    public WatchListViewResult(long id, WatchListWithUsersRecord watchlistUserViewRecord, List<WatchListWithMediaRecord> watchListMediaViewRecords) {
        this.id = id;
        this.watchlistUserViewRecord = watchlistUserViewRecord;
        this.watchListMediaViewRecords = watchListMediaViewRecords;
    }

    public long getId() {
        return id;
    }

    public WatchListWithUsersRecord getWatchlistUserViewRecord() {
        return watchlistUserViewRecord;
    }

    public void setWatchlistUserViewRecord(WatchListWithUsersRecord watchlistUserViewRecord) {
        this.watchlistUserViewRecord = watchlistUserViewRecord;
    }

    public List<WatchListWithMediaRecord> getWatchListMediaViewRecords() {
        return watchListMediaViewRecords;
    }

    public void setWatchListMediaViewRecords(List<WatchListWithMediaRecord> watchListMediaViewRecords) {
        this.watchListMediaViewRecords = watchListMediaViewRecords;
    }

    public void addWatchListMediaViewRecord(WatchListWithMediaRecord watchListWithMediaRecord) {
        this.watchListMediaViewRecords.add(watchListWithMediaRecord);
    }
}
