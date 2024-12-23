package net.joostvdg.wwi.media.impl;

import net.joostvdg.wwi.media.Movie;
import net.joostvdg.wwi.media.MovieService;
import net.joostvdg.wwi.model.tables.records.MoviesRecord;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import net.joostvdg.wwi.model.Tables;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {


    private final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);
    private final DSLContext create;

    public MovieServiceImpl(DSLContext create) {
        this.create = create;
    }

    @Override
    public List<Movie> findAll() {

        List<Movie> movies = new ArrayList<>();
        create.selectFrom(Tables.MOVIES).fetch().forEach(record -> {
            Movie movie = translateRecordToMovie(record);
            movies.add(movie);
        });

        return movies;
    }

    private Movie translateRecordToMovie(MoviesRecord moviesRecord) {

        Optional<String> optionalUrl = moviesRecord.getUrl() != null ? Optional.of(moviesRecord.getUrl()) : Optional.empty();

        return new Movie(
                moviesRecord.getId(),
                moviesRecord.getTitle(),
                moviesRecord.getPlatform(),
                moviesRecord.getDirector(),
                moviesRecord.getDurationInMinutes(),
                moviesRecord.getReleaseYear(),
                Arrays.stream(moviesRecord.getGenre()).collect(Collectors.toSet()),
                optionalUrl,
                MediaHelper.translateTags(moviesRecord.getTags())
        );
    }



    @Override
    public Optional<Movie> findById(int id) {
        MoviesRecord foundRecord = create.selectFrom(Tables.MOVIES).where(Tables.MOVIES.ID.eq(id)).fetch().getFirst();
        if (foundRecord != null) {
            return Optional.of(translateRecordToMovie(foundRecord));
        }
        return Optional.empty();
    }

    // TODO: is this unique enough?
    MoviesRecord findMovieByTitleAndYear(String title, int year) {
        return create.selectFrom(Tables.MOVIES).where(Tables.MOVIES.TITLE.eq(title).and(Tables.MOVIES.RELEASE_YEAR.eq(year))).fetchOne();
    }

    @Override
    public Movie save(Movie movie) {

        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }

        if (movie.title() == null || movie.title().isBlank()) {
            throw new IllegalArgumentException("Movie title cannot be null or empty");
        }

        // Verify if the movie already exists
        MoviesRecord foundRecord = findMovieByTitleAndYear(movie.title(), movie.releaseYear());
        if (foundRecord != null) {
            throw new IllegalArgumentException("Movie already exists (by title and year)");
        }

        logger.info("Translating Movie tags to JSON: {}", movie.tags());
        String tagsJson = "{}";
        if (movie.tags().isPresent()) {
            tagsJson = MediaHelper.translateTagsToJson(movie.tags().get());
        }
        logger.info("Tags JSON: {}", tagsJson);

        // Create a new movie
        logger.info("Creating new Movie: {}", movie.title());
        MoviesRecord newMovieRecord = create.insertInto(Tables.MOVIES)
            .set(Tables.MOVIES.TITLE, movie.title())
            .set(Tables.MOVIES.PLATFORM, movie.platform())
            .set(Tables.MOVIES.DIRECTOR, movie.director())
            .set(Tables.MOVIES.DURATION_IN_MINUTES, movie.durationInMinutes())
            .set(Tables.MOVIES.RELEASE_YEAR, movie.releaseYear())
            .set(Tables.MOVIES.GENRE, movie.genre().toArray(new String[0]))
            .set(Tables.MOVIES.URL, movie.url().orElse(null))
            .set(Tables.MOVIES.TAGS, JSONB.valueOf(tagsJson))
                .returning(Tables.MOVIES.ID)
                .fetchOne();

        if (newMovieRecord == null) {
            throw new IllegalStateException("Movie could not be created");
        }
        return findById(newMovieRecord.getId()).orElseThrow();
    }

    @Override
    public void deleteById(int id) {
        logger.info("Deleting Movie with id: {}", id);
        if (id < 1) {
            throw new IllegalArgumentException("Movie id cannot be 0 or negative");
        }
        create.deleteFrom(Tables.MOVIES).where(Tables.MOVIES.ID.eq(id)).execute();
    }

    // TODO: optimize this, to avoid duplication from Save method
    @Override
    public void update(Movie movieToUpdate) {
        if (movieToUpdate == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }

        if (movieToUpdate.title() == null || movieToUpdate.title().isBlank()) {
            throw new IllegalArgumentException("Movie title cannot be null or empty");
        }

        MoviesRecord foundRecord = findMovieByTitleAndYear(movieToUpdate.title(), movieToUpdate.releaseYear());
        if (foundRecord == null) {
            throw new IllegalArgumentException("Movie does not exist");
        }

        logger.info("Translating Movie tags to JSON: {}", movieToUpdate.tags());
        String tagsJson = "{}";
        if (movieToUpdate.tags().isPresent()) {
            tagsJson = MediaHelper.translateTagsToJson(movieToUpdate.tags().get());
        }
        logger.info("Tags JSON: {}", tagsJson);

        create.update(Tables.MOVIES)
            .set(Tables.MOVIES.TITLE, movieToUpdate.title())
            .set(Tables.MOVIES.PLATFORM, movieToUpdate.platform())
            .set(Tables.MOVIES.DIRECTOR, movieToUpdate.director())
            .set(Tables.MOVIES.DURATION_IN_MINUTES, movieToUpdate.durationInMinutes())
            .set(Tables.MOVIES.RELEASE_YEAR, movieToUpdate.releaseYear())
            .set(Tables.MOVIES.GENRE, movieToUpdate.genre().toArray(new String[0]))
            .set(Tables.MOVIES.URL, movieToUpdate.url().orElse(null))
            .set(Tables.MOVIES.TAGS, JSONB.valueOf(tagsJson))
            .where(Tables.MOVIES.ID.eq(foundRecord.getId()))
            .execute();
    }
}
