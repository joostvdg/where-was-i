package net.joostvdg.wwi.media.impl;

import net.joostvdg.wwi.media.Media;
import net.joostvdg.wwi.media.VideoGame;
import net.joostvdg.wwi.media.VideoGameService;
import net.joostvdg.wwi.model.Tables;
import net.joostvdg.wwi.model.tables.records.VideoGamesRecord;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoGameServiceImpl implements VideoGameService {
    private final DSLContext create;

    public VideoGameServiceImpl(DSLContext create) {
        this.create = create;
    }

    @Override
    public VideoGame addVideoGame(VideoGame videoGame) {
        if (videoGame == null) {
            throw new IllegalArgumentException("VideoGame cannot be null");
        }

        if (videoGame.title() == null || videoGame.title().isBlank()) {
            throw new IllegalArgumentException("VideoGame title cannot be null or empty");
        }

        VideoGame foundVideoGame = findVideoGameById(videoGame.id()).orElse(null);
        if (foundVideoGame != null) {
            throw new IllegalArgumentException("VideoGame with id " + videoGame.id() + " already exists");
        }

        String tagsJson = "{}";
        if (videoGame.tags().isPresent()) {
            tagsJson = MediaHelper.translateTagsToJson(videoGame.tags().get());
        }

        VideoGamesRecord newVideoGameRecord = create.insertInto(Tables.VIDEO_GAMES)
                .set(Tables.VIDEO_GAMES.TITLE, videoGame.title())
                .set(Tables.VIDEO_GAMES.GENRE, videoGame.genre().toArray(new String[0]))
                .set(Tables.VIDEO_GAMES.PLATFORM, videoGame.platform())
                .set(Tables.VIDEO_GAMES.PUBLISHER, videoGame.publisher())
                .set(Tables.VIDEO_GAMES.DEVELOPER, videoGame.developer())
                .set(Tables.VIDEO_GAMES.YEAR, videoGame.year())
                .set(Tables.VIDEO_GAMES.TAGS, JSONB.valueOf(tagsJson))
                .returning(Tables.VIDEO_GAMES.ID)
                .fetchOne();

        if (newVideoGameRecord == null) {
            throw new IllegalStateException("VideoGame could not be created");
        }
        var id = newVideoGameRecord.getId();
        VideoGame newVideoGame = findVideoGameById(id).orElse(null);
        if (newVideoGame == null) {
            throw new IllegalStateException("VideoGame could not be created (verification failed)");
        }
        return newVideoGame;
    }

    @Override
    public Optional<VideoGame> findVideoGameById(int id) {
        VideoGamesRecord videoGamesRecord = create.selectFrom(Tables.VIDEO_GAMES).where(Tables.VIDEO_GAMES.ID.eq(id)).fetchOne();
        if (videoGamesRecord != null) {
            VideoGame videoGame = translateRecordToVideoGame(videoGamesRecord);
            return Optional.of(videoGame);
        }
        return Optional.empty();
    }

    @Override
    public List<VideoGame> findAll() {
        List<VideoGamesRecord> videoGamesRecords = create.selectFrom(Tables.VIDEO_GAMES).fetch();
        List<VideoGame> videoGames = new ArrayList<>();
        for (VideoGamesRecord videoGamesRecord : videoGamesRecords) {
            VideoGame videoGame = translateRecordToVideoGame(videoGamesRecord);
            videoGames.add(videoGame);
        }
        return videoGames;
    }

    @Override
    public void updateVideoGame(VideoGame videoGame) {
        if (videoGame == null) {
            throw new IllegalArgumentException("VideoGame cannot be null");
        }

        if (videoGame.title() == null || videoGame.title().isBlank()) {
            throw new IllegalArgumentException("VideoGame title cannot be null or empty");
        }

        VideoGamesRecord foundRecord = create.selectFrom(Tables.VIDEO_GAMES).where(Tables.VIDEO_GAMES.ID.eq(videoGame.id())).fetchOne();
        if (foundRecord == null) {
            throw new IllegalArgumentException("VideoGame does not exist");
        }

        String tagsJson = "{}";
        if (videoGame.tags().isPresent()) {
            tagsJson = MediaHelper.translateTagsToJson(videoGame.tags().get());
        }

        create.update(Tables.VIDEO_GAMES)
            .set(Tables.VIDEO_GAMES.TITLE, videoGame.title())
            .set(Tables.VIDEO_GAMES.GENRE, videoGame.genre().toArray(new String[0]))
            .set(Tables.VIDEO_GAMES.PLATFORM, videoGame.platform())
            .set(Tables.VIDEO_GAMES.PUBLISHER, videoGame.publisher())
            .set(Tables.VIDEO_GAMES.DEVELOPER, videoGame.developer())
            .set(Tables.VIDEO_GAMES.YEAR, videoGame.year())
            .set(Tables.VIDEO_GAMES.TAGS, JSONB.valueOf(tagsJson))
            .where(Tables.VIDEO_GAMES.ID.eq(foundRecord.getId()))
            .execute();
    }

    @Override
    public Media translateViewRecordToVideoGame(Record watchListViewMediaRecord) {
        int id = watchListViewMediaRecord.get("media_id", Integer.class);
        String title = watchListViewMediaRecord.get("media_title", String.class);
        String platform = watchListViewMediaRecord.get("platform", String.class);
        Set<String> genre = Arrays.stream(watchListViewMediaRecord.get("genre", String[].class)).collect(Collectors.toSet());
        String publisher = watchListViewMediaRecord.get("publisher", String.class);
        String developer = watchListViewMediaRecord.get("developer", String.class);
        int year = watchListViewMediaRecord.get("year", Integer.class);

        JSONB tagsJSONB = watchListViewMediaRecord.get("tags", JSONB.class);
        Optional<Map<String, String>> tags = Optional.empty();
        if (tagsJSONB != null && !tagsJSONB.data().isBlank()) {
            tags = MediaHelper.translateTags(tagsJSONB);
        }

        return new VideoGame(id, title, platform, genre, publisher, developer, year, tags);
    }

    // TODO: is this the same as the record from translateViewRecordToVideoGame?
    private VideoGame translateRecordToVideoGame(VideoGamesRecord videoGamesRecord) {
        int id = videoGamesRecord.getId();
        String title = videoGamesRecord.getTitle();
        String platform = videoGamesRecord.getPlatform();
        Set<String> genre = new HashSet<>(Arrays.asList(videoGamesRecord.getGenre()));
        String publisher = videoGamesRecord.getPublisher();
        String developer = videoGamesRecord.getDeveloper();
        int year = videoGamesRecord.getYear();
        Optional<Map<String, String>> tags = Optional.empty();
        if (videoGamesRecord.getTags() != null) {
            tags = MediaHelper.translateTags(videoGamesRecord.getTags());
        }
        return new VideoGame(id, title, platform, genre, publisher, developer, year, tags);
    }
}
