package net.joostvdg.wwi.media.impl;

import net.joostvdg.wwi.media.Movie;
import net.joostvdg.wwi.media.MovieService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MovieServiceImpl implements MovieService {

    private final List<Movie> movieRepository = new ArrayList<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);


    @Override
    public List<Movie> findAll() {
        return new ArrayList<>(movieRepository);
    }

    @Override
    public Optional<Movie> findById(long id) {
        return movieRepository.stream().filter(movie -> movie.id() == id).findFirst();
    }

    @Override
    public Movie save(Movie movie) {

        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }

        if (movie.title() == null || movie.title().isBlank()) {
            throw new IllegalArgumentException("Movie title cannot be null or empty");
        }

        if (movieRepository.contains(movie)) {
            throw new IllegalArgumentException("Movie already exists");
        }

        Movie newMovie = new Movie(idCounter.getAndIncrement(), movie.title(), movie.platform(), movie.director(), movie.durationInMinutes(), movie.releaseYear(), movie.genre(), movie.url(), movie.tags());
        movieRepository.add(newMovie);
        return newMovie;
    }

    @Override
    public void deleteById(long id) {
        movieRepository.removeIf(movie -> movie.id() == id);
    }
}
