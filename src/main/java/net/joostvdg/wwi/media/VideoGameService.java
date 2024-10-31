package net.joostvdg.wwi.media;

import java.util.List;
import java.util.Optional;

public interface VideoGameService {
    VideoGame addVideoGame(VideoGame newVideoGame);

    Optional<VideoGame> findVideoGameById(long id);

    List<VideoGame> findAll();
}
