package net.joostvdg.wwi.media.impl;

import net.joostvdg.wwi.media.Movie;
import net.joostvdg.wwi.media.MovieService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MovieServiceImpl implements MovieService {

    private final List<Movie> movieRepository = new ArrayList<>();

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
        movieRepository.removeIf(existingMovie -> existingMovie.id() == movie.id());
        movieRepository.add(movie);
        return movie;
    }

    @Override
    public void deleteById(long id) {
        movieRepository.removeIf(movie -> movie.id() == id);
    }
}
