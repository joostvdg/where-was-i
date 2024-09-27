package net.joostvdg.wwi.media;

import java.util.List;
import java.util.Optional;

public interface MovieService {
    List<Movie> findAll();
    Optional<Movie> findById(long id);
    Movie save(Movie movie);
    void deleteById(long id);
}
