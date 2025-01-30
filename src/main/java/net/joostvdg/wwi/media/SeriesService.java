/* (C)2024 */
package net.joostvdg.wwi.media;

import java.util.List;
import java.util.Optional;
import org.jooq.Record;

public interface SeriesService {
  Series addSeries(Series newSeries);

  List<Series> findAll();

  Media translateViewRecordToSeries(Record watchListViewMediaRecord);

  Optional<Series> findById(int id);

  Series updateSeries(int id, Series updatedSeries);
}
