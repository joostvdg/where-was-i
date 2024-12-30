package net.joostvdg.wwi.watchlist;

import net.joostvdg.wwi.media.Media;
import net.joostvdg.wwi.user.User;

import java.util.List;
import java.util.Optional;

public interface WatchlistService {
    void addWatchList(WatchList watchList);

    Optional<WatchList> getWatchlistById(int id);

    List<WatchList> getWatchListsForUser(User user);

    List<WatchList> findSharedWith(User loggedInUser);

    void shareWatchlist(WatchList watchlist, User selectedUser);

    void addMedia(WatchList watchList, Media media);
}
