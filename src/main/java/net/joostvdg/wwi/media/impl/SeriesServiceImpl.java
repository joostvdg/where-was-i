package net.joostvdg.wwi.media.impl;

import net.joostvdg.wwi.media.Series;
import net.joostvdg.wwi.media.SeriesService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SeriesServiceImpl implements SeriesService {
    private final Set<Series> series;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public SeriesServiceImpl() {
        this.series = new HashSet<>();
    }

    @Override
    public Series addSeries(Series tvSeries) {

        if (tvSeries == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        if (tvSeries.title() == null || tvSeries.title().isBlank()) {
            throw new IllegalArgumentException("Series title cannot be null or empty");
        }

        if (series.contains(tvSeries)) {
            throw new IllegalArgumentException("Series already exists");
        }
        Series newSeries = new Series(idCounter.getAndIncrement(), tvSeries.title(), tvSeries.genre(), tvSeries.seasons(), tvSeries.platform(), tvSeries.url(), tvSeries.releaseYear(), tvSeries.endYear(), tvSeries.tags());
        series.add(newSeries);
        return newSeries;
    }

    @Override
    public List<Series> findAll() {
        return new ArrayList<>(series);
    }
}
