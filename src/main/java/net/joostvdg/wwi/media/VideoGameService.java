package net.joostvdg.wwi.media;

import org.jooq.Record;

import java.util.List;
import java.util.Optional;

public interface VideoGameService {
    VideoGame addVideoGame(VideoGame newVideoGame);

    Optional<VideoGame> findVideoGameById(int id);

    List<VideoGame> findAll();

    void updateVideoGame(VideoGame videoGame);

    Media translateViewRecordToVideoGame(Record watchListViewMediaRecord);

    Optional<VideoGame> findById(int id);
}
