package net.joostvdg.wwi.media;

import net.joostvdg.wwi.model.tables.records.WatchListWithMediaRecord;
import org.jooq.Record;

import java.util.List;
import java.util.Optional;

public interface MovieService {
    List<Movie> findAll();
    Optional<Movie> findById(int id);
    Movie save(Movie movie);
    void deleteById(int id);

    void update(Movie movieToUpdate);

    Media translateViewRecordToMovie(WatchListWithMediaRecord watchListViewMediaRecord);
}
