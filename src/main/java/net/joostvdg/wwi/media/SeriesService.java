package net.joostvdg.wwi.media;

import java.util.List;

public interface SeriesService {
    void addSeries(Series newSeries);

    List<Series> findAll();
}
