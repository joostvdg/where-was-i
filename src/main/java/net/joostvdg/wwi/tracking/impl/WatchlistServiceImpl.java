package net.joostvdg.wwi.tracking.impl;

import net.joostvdg.wwi.media.*;
import net.joostvdg.wwi.model.tables.records.WatchListRecord;
import net.joostvdg.wwi.model.tables.records.WatchListWithMediaRecord;
import net.joostvdg.wwi.model.tables.records.WatchListWithUsersRecord;
import net.joostvdg.wwi.tracking.WatchList;
import net.joostvdg.wwi.tracking.WatchlistService;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static net.joostvdg.wwi.model.Tables.*;

@Service
public class WatchlistServiceImpl implements WatchlistService {

    private final UserService userService;
    private final MovieService movieService;
    private final SeriesService seriesService;
    private final VideoGameService videoGameService;

    private final DSLContext create;

    private final Logger logger = LoggerFactory.getLogger(WatchlistServiceImpl.class);

    public WatchlistServiceImpl(UserService userService, MovieService movieService, SeriesService seriesService, VideoGameService videoGameService, DSLContext create) {
        this.userService = userService;
        this.movieService = movieService;
        this.seriesService = seriesService;
        this.videoGameService = videoGameService;
        this.create = create;
    }

    @Override
    public Set<WatchList> getWatchLists() {
        // TODO: I don't think this is called anymore
        return Collections.EMPTY_SET;
    }

    @Override
    @Transactional
    public void addWatchList(WatchList watchList) {
        if (watchList == null) {
            throw new IllegalArgumentException("Watchlist cannot be null");
        }

        // verify the watchlist does not already exist
        create.selectFrom(WATCH_LIST_WITH_USERS)
                .where(WATCH_LIST_WITH_USERS.NAME.eq(watchList.getName()))
                .fetchOptional()
                .ifPresent(record -> {
                    throw new IllegalArgumentException("Watchlist with name " + record.getName() + " already exists");
                });

        // verify the owner exists
        if (!userService.userExists(watchList.getOwner())) {
            throw new IllegalArgumentException("Owner does not exist");
        }

        var now = LocalDateTime.now();
        Long ownerId = (long) watchList.getOwner().id();

        // insert the watchlist

        // retrieve the id of the inserted watchlist
        WatchListRecord createdWatchList = create.insertInto(WATCH_LIST)
                .set(WATCH_LIST.NAME, watchList.getName())
                .set(WATCH_LIST.DESCRIPTION, watchList.getDescription())
                .set(WATCH_LIST.CREATED, now)
                .set(WATCH_LIST.LAST_EDIT, now)
                .set(WATCH_LIST.FAVORITE, watchList.isFavorite())
                .set(WATCH_LIST.OWNER_ID, ownerId)
                .returning(WATCH_LIST.ID).fetchOne();
        if (createdWatchList == null) {
            throw new IllegalStateException("Watchlist could not be created");
        }
        int watchListId = createdWatchList.getId();


        // insert the media items
        for (Media item : watchList.getItems()) {
            switch (item.getType()) {
                case Movie.TYPE -> create.insertInto(WATCH_LIST_MOVIES)
                        .set(WATCH_LIST_MOVIES.WATCH_LIST_ID, (long) watchListId)
                        .set(WATCH_LIST_MOVIES.MOVIE_ID, (long) item.getId())
                        .execute();
                case Series.TYPE -> create.insertInto(WATCH_LIST_SERIES)
                        .set(WATCH_LIST_SERIES.WATCH_LIST_ID, (long) watchList.getId())
                        .set(WATCH_LIST_SERIES.SERIES_ID, (long) item.getId())
                        .execute();
                case VideoGame.TYPE -> create.insertInto(WATCH_LIST_VIDEO_GAMES)
                        .set(WATCH_LIST_VIDEO_GAMES.WATCH_LIST_ID, (long) watchList.getId())
                        .set(WATCH_LIST_VIDEO_GAMES.VIDEO_GAME_ID, (long) item.getId())
                        .execute();
            }
        }

        // insert the read shared users
        for (User user : watchList.getReadShared()) {
            create.insertInto(WATCH_LIST_READ_SHARED)
                    .columns(WATCH_LIST_READ_SHARED.WATCH_LIST_ID, WATCH_LIST_READ_SHARED.USER_ID)
                    .values((long) watchListId, (long) user.id())
                    .execute();
        }

        // insert the write shared users
        for (User user : watchList.getWriteShared()) {
            create.insertInto(WATCH_LIST_WRITE_SHARED)
                    .columns(WATCH_LIST_WRITE_SHARED.WATCH_LIST_ID, WATCH_LIST_WRITE_SHARED.USER_ID)
                    .values((long) watchListId, (long) user.id())
                    .execute();
        }
        // TODO: update the views?
    }

    private Result<WatchListWithMediaRecord> fetchWatchListMediaByWatchListId(int watchListId) {
        return create.selectFrom(WATCH_LIST_WITH_MEDIA)
                .where(WATCH_LIST_WITH_MEDIA.WATCH_LIST_ID.eq(watchListId))
                .fetch();
    }

    private WatchListWithUsersRecord getWatchListWithUsersById(int id) {
        return create.selectFrom(WATCH_LIST_WITH_USERS)
                .where(WATCH_LIST_WITH_USERS.WATCH_LIST_ID.eq(id))
                .fetchOne();
    }

    private Result<WatchListWithUsersRecord> getWatchListsWithUsersByOwnerId(int ownerId) {
        return create.
                selectFrom(WATCH_LIST_WITH_USERS)
                .where(WATCH_LIST_WITH_USERS.OWNER_ID.eq((long) ownerId))
                .fetch();
    }

    @Override
    public Optional<WatchList> getWatchlistById(int id) {
        // Collect WatchList with users
        WatchListWithUsersRecord watchlistUserViewRecord = getWatchListWithUsersById(id);
        if (watchlistUserViewRecord == null) {
            return Optional.empty();
        }

        // Collect WatchList with media
        Result<WatchListWithMediaRecord> watchListMediaViewRecords = fetchWatchListMediaByWatchListId(id);
        List<WatchListWithMediaRecord> watchListMediaViewRecordList = watchListMediaViewRecords.subList(0, watchListMediaViewRecords.size());

        // Translate to WatchList
        return Optional.of(translateViewRecordsToWatchList(watchlistUserViewRecord, watchListMediaViewRecordList));
    }

    private WatchList translateViewRecordsToWatchList(WatchListWithUsersRecord watchlistUserViewRecord, List<WatchListWithMediaRecord> watchListMediaViewRecords) {
        int id = watchlistUserViewRecord.get(WATCH_LIST_WITH_USERS.WATCH_LIST_ID);
        String name = watchlistUserViewRecord.get(WATCH_LIST_WITH_USERS.NAME);
        String description = watchlistUserViewRecord.get(WATCH_LIST_WITH_USERS.DESCRIPTION);
        Instant created = watchlistUserViewRecord.get(WATCH_LIST_WITH_USERS.CREATED).toInstant(ZoneOffset.UTC);
        Instant lastEdit = watchlistUserViewRecord.get(WATCH_LIST_WITH_USERS.LAST_EDIT).toInstant(ZoneOffset.UTC);
        boolean favorite = watchlistUserViewRecord.get(WATCH_LIST_WITH_USERS.FAVORITE);

        // Reconstruct Media
        Set<Media> items = new HashSet<>(); // A set of media items (Movie, Series, VideoGame)
        // For each record in watchListMediaViewRecords, translate to Media of the correct type and add to items
        for (WatchListWithMediaRecord watchListRecord : watchListMediaViewRecords) {
            // Switch on type and translate to Media
            Media item = switch (watchListRecord.get(WATCH_LIST_WITH_MEDIA.MEDIA_TYPE)) {
                case Movie.TYPE -> movieService.translateViewRecordToMovie(watchListRecord);
                case Series.TYPE -> seriesService.translateViewRecordToSeries(watchListRecord);
                case VideoGame.TYPE -> videoGameService.translateViewRecordToVideoGame(watchListRecord);
                default -> throw new IllegalArgumentException("Unknown media type");
            };
            if (item != null) {
                items.add(item);
            }
        }

        // Reconstruct Users
        User owner = userService.translateViewRecordToUser(watchlistUserViewRecord, "owner");
        // TODO: implement readShared and writeShared
        Set<User> readShared = new HashSet<>();
        Set<User> writeShared = new HashSet<>();

        return new WatchList(id, items, owner, readShared, writeShared, name, description, created, lastEdit, favorite);
    }

    @Override
    // TODO: re-enable this when we support Progress via DB
    public Set<Progress> getProgressForWatchlist(WatchList watchList) {
//        // retrieve the user from the UserService
//        var user = userService.getUserForUsername(watchList.getOwner().username()).orElseThrow(() -> new IllegalArgumentException("User does not exist"));
//
//        // retrieve the progress from the user
//        var progresses = user.progress();
//        // filter the progresses to only include the ones that are part of the watchlist
//        logger.info("Retrieving progresses for user {}: {}", user.username(), progresses);
//
//        return progresses.stream()
//                .filter(progress -> watchList.getItems().contains(progress.getMedia()))
//                .collect(Collectors.toSet());
        return Collections.EMPTY_SET;
    }

    @Override
    public List<WatchList> getWatchListsForUser(User user) {
        Result<WatchListWithUsersRecord> watchListRecords = getWatchListsWithUsersByOwnerId(user.id());
        List<WatchList> watchLists = new ArrayList<>();
        for (WatchListWithUsersRecord watchListRecord : watchListRecords) {
            int id = watchListRecord.get(WATCH_LIST_WITH_USERS.WATCH_LIST_ID);
            Result<WatchListWithMediaRecord> watchListMediaViewRecords = fetchWatchListMediaByWatchListId(id);
            List<WatchListWithMediaRecord> watchListMediaViewRecordList = watchListMediaViewRecords.subList(0, watchListMediaViewRecords.size());
            watchLists.add(translateViewRecordsToWatchList(watchListRecord, watchListMediaViewRecordList));
        }
        return watchLists;
    }

    @Override
    public List<WatchList> findSharedWith(User user) {
        // retrieve the watchlist IDs of those shared with the user
        // So that should be from the WATCH_LIST_READ_SHARED and WATCH_LIST_WRITE_SHARED tables

        // retrieve WatchLists from the WATCH_LIST_READ_SHARED table
        Result<Record> readSharedRecords = create.select()
                .from(WATCH_LIST_READ_SHARED)
                .where(WATCH_LIST_READ_SHARED.USER_ID.eq((long) user.id()))
                .fetch();

        // retrieve WatchLists from the WATCH_LIST_WRITE_SHARED table
        Result<Record> writeSharedRecords = create.select()
                .from(WATCH_LIST_WRITE_SHARED)
                .where(WATCH_LIST_WRITE_SHARED.USER_ID.eq((long) user.id()))
                .fetch();

        // combine the two results, to get the unique set of WatchList IDs
        Set<Integer> watchListIds = new HashSet<>();
        for (Record readSharedRecord : readSharedRecords) {
            watchListIds.add(Math.toIntExact(readSharedRecord.get(WATCH_LIST_READ_SHARED.WATCH_LIST_ID)));
        }
        for (Record writeSharedRecord : writeSharedRecords) {
            watchListIds.add(Math.toIntExact(writeSharedRecord.get(WATCH_LIST_WRITE_SHARED.WATCH_LIST_ID)));
        }

        // retrieve the WatchLists via the existing methods
        List<WatchList> watchLists = new ArrayList<>();
        // need to do a fetch on WATCH_LIST_WITH_USERS and WATCH_LIST_WITH_MEDIA views for the range of IDs

        Result<WatchListWithUsersRecord> watchListWithUsersRecordResult = create.
            selectFrom(WATCH_LIST_WITH_USERS)
            .where(WATCH_LIST_WITH_USERS.WATCH_LIST_ID.in(watchListIds))
            .fetch();

        Result<WatchListWithMediaRecord> watchListMediaRecords = create
            .selectFrom(WATCH_LIST_WITH_MEDIA)
            .where(WATCH_LIST_WITH_MEDIA.WATCH_LIST_ID.in(watchListIds))
            .fetch();

        Map<Long, WatchListViewResult> watchListMap = new HashMap<>();
        for (WatchListWithUsersRecord watchListWithUsersRecord : watchListWithUsersRecordResult) {
            long watchListId = watchListWithUsersRecord.get(WATCH_LIST_WITH_USERS.WATCH_LIST_ID);
            WatchListViewResult watchListViewResult = watchListMap.get(watchListId);
            watchListViewResult.setWatchlistUserViewRecord(watchListWithUsersRecord);
            watchListMap.put(watchListId, watchListViewResult);
        }

        for (WatchListWithMediaRecord watchListMediaRecord : watchListMediaRecords) {
            long watchListId = watchListMediaRecord.get(WATCH_LIST_WITH_MEDIA.WATCH_LIST_ID);
            WatchListViewResult watchListViewResult = watchListMap.get(watchListId);
            watchListViewResult.addWatchListMediaViewRecord(watchListMediaRecord);
        }

        for (WatchListViewResult watchListViewResult : watchListMap.values()) {
            watchLists.add(translateViewRecordsToWatchList(watchListViewResult.getWatchlistUserViewRecord(), watchListViewResult.getWatchListMediaViewRecords()));
        }

        return watchLists;

    }

    @Override
    public void shareWatchlist(WatchList watchlist, User selectedUser) {
        var loggedInUser = userService.getLoggedInUser();
        if (loggedInUser.username().equals(selectedUser.username())) {
            throw new IllegalArgumentException("Cannot share with yourself");
        }

        if (!watchlist.getOwner().username().equals(loggedInUser.username())) {
            throw new IllegalArgumentException("Cannot share a watchlist you do not own");
        }

        // verify the watchlist exists by fetching it
        var watchlistOptional = getWatchlistById(watchlist.getId());
        if (watchlistOptional.isEmpty()) {
            throw new IllegalArgumentException("Watchlist does not exist");
        }

        // verify the user exists
        if (!userService.userExists(selectedUser)) {
            throw new IllegalArgumentException("User does not exist");
        }

        // add the user to the readShared list
        watchlist.getReadShared().add(selectedUser);
        // add the user to the writeShared list
        create.insertInto(WATCH_LIST_WRITE_SHARED)
                .columns(WATCH_LIST_WRITE_SHARED.WATCH_LIST_ID, WATCH_LIST_WRITE_SHARED.USER_ID)
                .values((long) watchlist.getId(), (long) selectedUser.id())
                .execute();
    }


}
