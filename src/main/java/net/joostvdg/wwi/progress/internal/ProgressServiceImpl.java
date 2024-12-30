package net.joostvdg.wwi.progress.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.joostvdg.wwi.media.*;
import net.joostvdg.wwi.media.Movie;
import net.joostvdg.wwi.media.MovieProgress;
import net.joostvdg.wwi.media.Series;
import net.joostvdg.wwi.media.SeriesProgress;
import net.joostvdg.wwi.media.VideoGame;
import net.joostvdg.wwi.media.VideoGameProgress;
import net.joostvdg.wwi.model.wwi_progress.Tables;
import net.joostvdg.wwi.model.wwi_progress.tables.records.AllProgressRecord;
import net.joostvdg.wwi.progress.ProgressService;
import net.joostvdg.wwi.user.User;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ProgressServiceImpl implements ProgressService {

    private final DSLContext create;

    private final Logger logger = LoggerFactory.getLogger(ProgressServiceImpl.class);

    public ProgressServiceImpl(DSLContext create) {
        this.create = create;
    }

    @Override
    public Set<Progress> getProgressForUserAndMedia(int userId, Set<Media> items) {

        // Collect Progress for User
        Result<AllProgressRecord> userProgressRecords = create
                .selectFrom(Tables.ALL_PROGRESS)
                .where(Tables.ALL_PROGRESS.USER_ID.eq(userId))
                .fetch();

        List<AllProgressRecord> filteredProgressRecords = new ArrayList<>();
        // Filter Progress for Media that are part of the WatchList's items
        for (AllProgressRecord userProgressRecord : userProgressRecords) {
            for (Media item : items) {
                if (userProgressRecord.getMediaId().equals(item.getId())) {
                    filteredProgressRecords.add(userProgressRecord);
                }
            }
        }

        Set<Progress> progress = new HashSet<>();
        for (AllProgressRecord filteredProgressRecord : filteredProgressRecords) {
            var progressItem = translateRecordToProgress(filteredProgressRecord);
            progress.add(progressItem);
        }

        return progress;
    }

    @Override
    public void createMovieProgressForUser(User user, Movie movie) {
        logger.info("Creating Progress for User {} and Movie {}", user.username(), movie.getTitle());
        String progressJson = generateDefaultProgressForMovie();

        create.insertInto(Tables.MOVIE_PROGRESS)
                .set(Tables.MOVIE_PROGRESS.USER_ID, user.id())
                .set(Tables.MOVIE_PROGRESS.MOVIE_ID, movie.getId())
                .set(Tables.MOVIE_PROGRESS.PROGRESS, JSONB.valueOf(progressJson))
                .set(Tables.MOVIE_PROGRESS.FAVORITE, false)
                .set(Tables.MOVIE_PROGRESS.FINISHED, false)
                .execute();
    }

    private static String generateDefaultProgressForMovie() {
        Map<String, Integer> movieProgress = Map.of("Minutes Watched", 0);
        String progressJson = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            progressJson = objectMapper.writeValueAsString(movieProgress);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return progressJson;
    }

    @Override
    public void createSeriesProgressForUser(User user, Series series) {
        logger.info("Creating Progress for User {} and Series {}", user.username(), series.getTitle());
        String progressJson = generateProgressForSeries(series);

        create.insertInto(Tables.SERIES_PROGRESS)
                .set(Tables.SERIES_PROGRESS.USER_ID, user.id())
                .set(Tables.SERIES_PROGRESS.SERIES_ID, series.getId())
                .set(Tables.SERIES_PROGRESS.PROGRESS, JSONB.valueOf(progressJson))
                .set(Tables.SERIES_PROGRESS.FAVORITE, false)
                .set(Tables.SERIES_PROGRESS.FINISHED, false)
                .execute();
    }

    private static String generateProgressForSeries(Series series) {
        Map<String, Integer> seriesProgress = new HashMap<>();
        series.seasons().forEach((season, episodes) -> seriesProgress.put(season, 0));
        String progressJson = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            progressJson = objectMapper.writeValueAsString(seriesProgress);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return progressJson;
    }

    @Override
    public void createVideoGameProgressForUser(User user, VideoGame videoGame) {
        logger.info("Creating Progress for User {} and VideoGame {}", user.username(), videoGame.getTitle());
        String progressJson = genereateDefaultProgressJSONForVideoGame();

        create.insertInto(Tables.VIDEO_GAME_PROGRESS)
                .set(Tables.VIDEO_GAME_PROGRESS.USER_ID, user.id())
                .set(Tables.VIDEO_GAME_PROGRESS.VIDEO_GAME_ID, videoGame.getId())
                .set(Tables.VIDEO_GAME_PROGRESS.PROGRESS, JSONB.valueOf(progressJson))
                .set(Tables.VIDEO_GAME_PROGRESS.FAVORITE, false)
                .set(Tables.VIDEO_GAME_PROGRESS.FINISHED, false)
                .execute();
    }

    @Override
    public void updateProgress(Progress updatedProgress) {
        // Determine the type of Progress
        switch (updatedProgress ) {
            case MovieProgress movieProgress:
                updateMovieProgress(movieProgress);
                break;
            case SeriesProgress seriesProgress:
                updateSeriesProgress(seriesProgress);
                break;
            case VideoGameProgress videoGameProgress:
                updateVideoGameProgress(videoGameProgress);
                break;
        }

    }

    private void updateVideoGameProgress(VideoGameProgress updatedProgress) {
        // verify the progress exists
        var progressRecord = create
                .selectFrom(Tables.VIDEO_GAME_PROGRESS)
                .where(Tables.VIDEO_GAME_PROGRESS.ID.eq(updatedProgress.getId()))
                .fetchOne();

        if (progressRecord == null) {
            throw new IllegalArgumentException("Progress does not exist");
        }

        String progressJSON = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            progressJSON = objectMapper.writeValueAsString(updatedProgress.getProgress());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        create.update(Tables.VIDEO_GAME_PROGRESS)
                .set(Tables.VIDEO_GAME_PROGRESS.PROGRESS, JSONB.valueOf(progressJSON))
                .set(Tables.VIDEO_GAME_PROGRESS.FAVORITE, updatedProgress.isFavorite())
                .set(Tables.VIDEO_GAME_PROGRESS.FINISHED, updatedProgress.isFinished())
                .where(Tables.VIDEO_GAME_PROGRESS.ID.eq(updatedProgress.getId()))
                .execute();
    }

    private void updateSeriesProgress(SeriesProgress updatedProgress) {
        // verify the progress exists
        var progressRecord = create
                .selectFrom(Tables.SERIES_PROGRESS)
                .where(Tables.SERIES_PROGRESS.ID.eq(updatedProgress.getId()))
                .fetchOne();

        if (progressRecord == null) {
            throw new IllegalArgumentException("Progress does not exist");
        }

        String progressJSON = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            progressJSON = objectMapper.writeValueAsString(updatedProgress.getProgress());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        create.update(Tables.SERIES_PROGRESS)
                .set(Tables.SERIES_PROGRESS.PROGRESS, JSONB.valueOf(progressJSON))
                .set(Tables.SERIES_PROGRESS.FAVORITE, updatedProgress.isFavorite())
                .set(Tables.SERIES_PROGRESS.FINISHED, updatedProgress.isFinished())
                .where(Tables.SERIES_PROGRESS.ID.eq(updatedProgress.getId()))
                .execute();
    }

    private void updateMovieProgress(MovieProgress updatedProgress) {
        // verify the progress exists
        var progressRecord = create
                .selectFrom(Tables.MOVIE_PROGRESS)
                .where(Tables.MOVIE_PROGRESS.ID.eq(updatedProgress.getId()))
                .fetchOne();

        if (progressRecord == null) {
            throw new IllegalArgumentException("Progress does not exist");
        }

        String progressJSON = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            progressJSON = objectMapper.writeValueAsString(updatedProgress.getProgress());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        create.update(Tables.MOVIE_PROGRESS)
                .set(Tables.MOVIE_PROGRESS.PROGRESS, JSONB.valueOf(progressJSON))
                .set(Tables.MOVIE_PROGRESS.FAVORITE, updatedProgress.isFavorite())
                .set(Tables.MOVIE_PROGRESS.FINISHED, updatedProgress.isFinished())
                .where(Tables.MOVIE_PROGRESS.ID.eq(updatedProgress.getId()))
                .execute();
    }

    private static String genereateDefaultProgressJSONForVideoGame() {
        Map<String, Integer> videoGameProgress = new HashMap<>();
        videoGameProgress.put("main", 0);
        videoGameProgress.put("side", 0);
        videoGameProgress.put("collectibles", 0);
        videoGameProgress.put("achievements", 0);

        String progressJson = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            progressJson = objectMapper.writeValueAsString(videoGameProgress);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return progressJson;
    }

    private Progress translateRecordToProgress(AllProgressRecord rec) {
        int mediaId = rec.getMediaId();
        String title = rec.getMediaTitle();
        String platform = rec.getMediaPlatform();
        Set<String> genre = Set.of();
        if (rec.getMediaGenre() != null && rec.getMediaGenre().length > 0) {
            genre = Set.of(rec.getMediaGenre());
        }
        String director = rec.getMediaDirector();
        int releaseYear = rec.getMediaReleaseYear();
        int endYear = 0;
        if (rec.getMediaEndYear() != null) {
            endYear = rec.getMediaEndYear();
        }
        int durationInMinutes = 0;
        if (rec.getMediaDurationInMinutes() != null) {
            durationInMinutes = rec.getMediaDurationInMinutes();
        }
        Optional<String> optionalUrl = Optional.empty();
        if (rec.getMediaUrl() != null) {
            optionalUrl = Optional.of(rec.getMediaUrl());
        }
        String developer = rec.getMediaDeveloper();
        String publisher = rec.getMediaPublisher();

        Map<String, Integer> seasons = new HashMap<>();
        if (rec.getMediaSeasons() != null && !rec.getMediaSeasons().data().isBlank()) {
            String seasonsJSON = rec.getMediaSeasons().data();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                seasons = objectMapper.readValue(seasonsJSON, new TypeReference<Map<String,Integer>>(){});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        Map<String, String> tags = new HashMap<>();
        if (rec.getMediaTags() != null && !rec.getMediaTags().data().isBlank()) {
            String tagsJSON = rec.getMediaTags().data();
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                tags = objectMapper.readValue(tagsJSON, new TypeReference<Map<String,String>>(){});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }


        switch (rec.getMediaType()) {
            case Movie.TYPE:
                // int id, String title, String platform,String director,int durationInMinutes,int releaseYear,Set<String> genre,Optional<String> url, Optional<Map<String, String>> tags
                Movie movie = new Movie(mediaId, title, platform, director, durationInMinutes, releaseYear, genre, optionalUrl, Optional.of(tags));

                Map<String, Integer> movieProgress = parseProgress(rec.getProgress());

                // long id, Movie movie, Map<String, Integer> progress, boolean favorite, boolean finished
                return new MovieProgress(rec.getProgressId(), movie, movieProgress, rec.getFavorite(), rec.getFinished());
            case Series.TYPE:
                // int id,String title,Set<String> genre,Map<String, Integer> seasons,String platform, Optional<String> url, Optional<LocalDate> releaseYear, Optional<LocalDate> endYear,Optional<Map<String, String>> tags
                Series series = new Series(mediaId, title, genre, seasons, platform, optionalUrl, Optional.of(LocalDate.of(releaseYear, 1, 1)), Optional.of(LocalDate.of(endYear, 1, 1)), Optional.of(tags));

                Map<String, Integer> seriesProgress = parseProgress(rec.getProgress());

                //         long id,
                //        boolean finished, // Indicates if the series is finished
                //        Series series, // The series being tracked
                //        Map<String, Integer> progress, // Progress map (e.g., "Season 1" -> 5, meaning 5 episodes watched)
                //        boolean favorite
                return new SeriesProgress(rec.getProgressId(), rec.getFinished(), series, seriesProgress, rec.getFavorite());
            case VideoGame.TYPE:
                // int id, String title, String platform, Set<String> genre, String publisher, String developer, int year, Optional<Map<String, String>> tags
                VideoGame videoGame = new VideoGame(mediaId, title, platform, genre, publisher, developer, releaseYear, Optional.of(tags));

                Map<String, Integer> videoGameProgress = parseProgress(rec.getProgress());

                //         long id,
                //        Map<String, Integer> progress, // Map of progress for each section of the game
                //        boolean finished, // Indicates if the series is finished
                //        VideoGame videoGame, // The series being tracked
                //        boolean favorite
                return new VideoGameProgress(rec.getProgressId(), videoGameProgress, rec.getFinished(), videoGame, rec.getFavorite());

            default:
                throw new IllegalArgumentException("Unknown Media Type");
        }
    }

    private Map<String, Integer> parseProgress(JSONB progress) {
        Map<String, Integer> progressMap = new HashMap<>();

        String jsonData = progress.data();

        if (!jsonData.isBlank()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                progressMap = objectMapper.readValue(jsonData, new TypeReference<Map<String,Integer>>(){});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }


        return progressMap;
    }
}
