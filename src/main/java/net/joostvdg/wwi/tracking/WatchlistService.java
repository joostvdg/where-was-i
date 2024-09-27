package net.joostvdg.wwi.tracking;

import net.joostvdg.wwi.media.Progress;

import java.util.Optional;
import java.util.Set;

public interface WatchlistService {
    Set<WatchList> getWatchLists();

    void addWatchList(WatchList watchList);

    Optional<WatchList> getWatchlistById(Long id);

    Set<Progress> getProgressForWatchlist(WatchList watchList);
}
