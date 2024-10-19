package net.joostvdg.wwi.tracking.impl;

import net.joostvdg.wwi.media.Progress;
import net.joostvdg.wwi.tracking.WatchList;
import net.joostvdg.wwi.tracking.WatchlistService;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class WatchlistServiceImpl implements WatchlistService {

    private final Set<WatchList> watchLists;
    private final UserService userService;

    private final AtomicInteger idCounter = new AtomicInteger(1);

    private final Logger logger = LoggerFactory.getLogger(WatchlistServiceImpl.class);

    public WatchlistServiceImpl(UserService userService) {
        this.userService = userService;
        this.watchLists = new HashSet<>();
    }

    @Override
    public Set<WatchList> getWatchLists() {
        return Collections.unmodifiableSet(watchLists);
    }

    @Override
    public void addWatchList(WatchList watchList) {
        // TODO: replace when using a database
//        private final Set<Media> items; // A set of media items (Movie, Series, VideoGame)
//        private final User owner;
//        private final Set<User> readShared; // Users who can read this list
//        private final Set<User> writeShared; // Users who can modify this list
//        private String name;
//        private String description;
//        private Instant created;
//        private Instant lastEdit;
//        private boolean favorite;
        WatchList watchListWithId = new WatchList(idCounter.getAndIncrement(), watchList.getItems(), watchList.getOwner(), watchList.getReadShared(), watchList.getWriteShared(), watchList.getName(), watchList.getDescription(), Instant.now(), Instant.now(), watchList.isFavorite());
        if (!watchLists.add(watchListWithId)) {
            throw new IllegalStateException("Watchlist already exists");
        }
    }

    @Override
    public Optional<WatchList> getWatchlistById(Long id) {
        return watchLists.stream()
                .filter(watchList -> watchList.getId() == id)
                .findFirst();
    }

    @Override
    public Set<Progress> getProgressForWatchlist(WatchList watchList) {
        // retrieve the user from the UserService
        var user = userService.getLoggedInUser();

        // retrieve the progress from the user
        var progresses = user.progress();
        // filter the progresses to only include the ones that are part of the watchlist
        logger.info("Retrieving progresses for user {}: {}", user.username(), progresses);

        return progresses.stream()
                .filter(progress -> watchList.getItems().contains(progress.getMedia()))
                .collect(Collectors.toSet());
    }

    @Override
    public List<WatchList> getWatchListsForUser(User user) {
        return watchLists.stream()
                .filter(watchList -> watchList.getOwner().username().equals(user.username()))
                .collect(Collectors.toList());
    }
}
