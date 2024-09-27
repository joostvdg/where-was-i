package net.joostvdg.wwi.media.impl;

import net.joostvdg.wwi.media.Series;
import net.joostvdg.wwi.media.SeriesService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class SeriesServiceImpl implements SeriesService {
    private final Set<Series> series;

    public SeriesServiceImpl() {
        this.series = new HashSet<>();
    }

    @Override
    public void addSeries(Series newSeries) {
        series.add(newSeries);
    }
}
