package net.joostvdg.wwi.media;

import org.jooq.Record;

import java.util.List;

public interface SeriesService {
    Series addSeries(Series newSeries);

    List<Series> findAll();

    Media translateViewRecordToSeries(Record watchListViewMediaRecord);
}
