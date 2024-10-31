package net.joostvdg.wwi.media.impl;

import net.joostvdg.wwi.media.VideoGame;
import net.joostvdg.wwi.media.VideoGameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class VideoGameServiceImpl implements VideoGameService {

    private final Set<VideoGame> videoGames;
    private final Logger logger = LoggerFactory.getLogger(VideoGameServiceImpl.class);
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public VideoGameServiceImpl() {
        this.videoGames = new HashSet<>();
    }


    @Override
    public VideoGame addVideoGame(VideoGame videoGame) {
        VideoGame newVideoGame = videoGame;
        if (videoGame.id() == 0) {
            newVideoGame = new VideoGame(idCounter.getAndIncrement(), videoGame.title(), videoGame.platform(), videoGame.genre(), videoGame.publisher(), videoGame.developer(), videoGame.year(), videoGame.tags());
        } else if (videoGames.stream().anyMatch(game -> game.id() == videoGame.id())) {
            logger.error("VideoGame already exists");
            throw new IllegalStateException("VideoGame already exists");
        }
        videoGames.add(newVideoGame);
        return newVideoGame;
    }

    @Override
    public Optional<VideoGame> findVideoGameById(long id) {
        return videoGames.stream()
                .filter(videoGame -> videoGame.id() == id)
                .findFirst();
    }

    @Override
    public List<VideoGame> findAll() {
        return new ArrayList<>(videoGames);
    }
}
