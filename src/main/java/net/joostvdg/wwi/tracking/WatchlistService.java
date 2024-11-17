package net.joostvdg.wwi.tracking;

import net.joostvdg.wwi.media.Progress;
import net.joostvdg.wwi.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WatchlistService {
    Set<WatchList> getWatchLists();

    void addWatchList(WatchList watchList);

    Optional<WatchList> getWatchlistById(Long id);

    Set<Progress> getProgressForWatchlist(WatchList watchList);

    List<WatchList> getWatchListsForUser(User user);

    List<WatchList> findSharedWith(User loggedInUser);

    void shareWatchlist(WatchList watchlist, User selectedUser);
}
