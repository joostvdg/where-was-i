package net.joostvdg.wwi.media;

import java.util.Optional;

public interface VideoGameService {
    void addVideoGame(VideoGame newVideoGame);

    Optional<VideoGame> findVideoGameById(long id);
}
