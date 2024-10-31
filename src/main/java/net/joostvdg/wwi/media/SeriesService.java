package net.joostvdg.wwi.media;

import java.util.List;

public interface SeriesService {
    Series addSeries(Series newSeries);

    List<Series> findAll();
}
