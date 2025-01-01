/* (C)2024 */
package net.joostvdg.wwi.media.internal;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import net.joostvdg.wwi.media.Media;
import net.joostvdg.wwi.media.Series;
import net.joostvdg.wwi.media.SeriesService;
import net.joostvdg.wwi.model.wwi_media.Tables;
import net.joostvdg.wwi.model.wwi_media.tables.records.SeriesRecord;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.Record;
import org.springframework.stereotype.Service;

@Service
public class SeriesServiceImpl implements SeriesService {

  private final DSLContext create;

  public SeriesServiceImpl(DSLContext create) {
    this.create = create;
  }

  @Override
  public Series addSeries(Series tvSeries) {

    if (tvSeries == null) {
      throw new IllegalArgumentException("Series cannot be null");
    }

    if (tvSeries.title() == null || tvSeries.title().isBlank()) {
      throw new IllegalArgumentException("Series title cannot be null or empty");
    }

    Series foundSeries = findSeriesById(tvSeries.id());
    if (foundSeries != null) {
      throw new IllegalArgumentException("Series with id " + tvSeries.id() + " already exists");
    }

    String tagsJson = "{}";
    if (tvSeries.tags().isPresent()) {
      tagsJson = MediaHelper.translateTagsToJson(tvSeries.tags().get());
    }

    SeriesRecord newSeriesRecord =
        create
            .insertInto(Tables.SERIES)
            .set(Tables.SERIES.TITLE, tvSeries.title())
            .set(Tables.SERIES.GENRE, tvSeries.genre().toArray(new String[0]))
            .set(
                Tables.SERIES.SEASONS,
                JSONB.valueOf(MediaHelper.translateSeanonsToJson(tvSeries.seasons())))
            .set(Tables.SERIES.PLATFORM, tvSeries.platform())
            .set(Tables.SERIES.URL, tvSeries.url().orElse(null))
            .set(
                Tables.SERIES.RELEASE_YEAR,
                tvSeries.releaseYear().map(LocalDate::getYear).orElse(null))
            .set(Tables.SERIES.END_YEAR, tvSeries.endYear().map(LocalDate::getYear).orElse(null))
            .set(Tables.SERIES.TAGS, JSONB.valueOf(tagsJson))
            .returning(Tables.SERIES.ID)
            .fetchOne();

    if (newSeriesRecord == null) {
      throw new IllegalStateException("Series could not be created");
    }

    var id = newSeriesRecord.getId();
    Series newSeries = findSeriesById(id);
    if (newSeries == null) {
      throw new IllegalStateException("Series could not be created (verification failed)");
    }

    return newSeries;
  }

  private Series findSeriesById(int id) {
    SeriesRecord foundRecord =
        create.selectFrom(Tables.SERIES).where(Tables.SERIES.ID.eq(id)).fetchOne();
    if (foundRecord == null) {
      return null;
    }
    return translateRecordToSeries(foundRecord);
  }

  @Override
  public List<Series> findAll() {
    List<SeriesRecord> seriesRecords = create.selectFrom(Tables.SERIES).fetch();
    List<Series> seriesList = new ArrayList<>();
    for (SeriesRecord seriesRecord : seriesRecords) {
      Series series = translateRecordToSeries(seriesRecord);
      seriesList.add(series);
    }
    return seriesList;
  }

  @Override
  public Media translateViewRecordToSeries(Record record) {
    //        'series' AS media_type,
    //        s.id AS media_id,
    //                s.title AS media_title,
    //        s.seasons,
    //                s.release_year,
    //                s.end_year,
    //                NULL::VARCHAR AS movie_director,
    //        NULL::INTEGER AS movie_duration_in_minutes,
    //        NULL::VARCHAR AS publisher,
    //        NULL::VARCHAR AS developer,
    //        NULL::INTEGER AS year
    //        int id,
    //        String title,
    //        Set<String> genre,
    //        Map<String, Integer> seasons, // Map of season name (e.g., "Season 1") to the number
    // of episodes
    //        String platform, // e.g., Netflix, HBO
    //        Optional<String> url, // URL for the series, optional
    //        Optional<LocalDate> releaseYear, // Optional release year
    //        Optional<LocalDate> endYear, // Optional end year
    //        Optional<Map<String, String>> tags // Optional tags (e.g., {"Director": "John Doe",
    // "Country": "USA"})
    int id = record.get("media_id", Integer.class);
    String title = record.get("media_title", String.class);
    String seasonJSON = record.get("seasons", JSONB.class).toString();
    String platform = record.get("platform", String.class);

    Set<String> genre =
        Arrays.stream(record.get("genre", String[].class)).collect(Collectors.toSet());
    Map<String, Integer> seasons = new HashMap<>();

    if (!seasonJSON.isBlank()) {
      seasons = MediaHelper.translateJsonToSeasons(seasonJSON);
    }

    Optional<String> optionalUrl = Optional.empty();
    if (record.get("url", String.class) != null && !record.get("url", String.class).isBlank()) {
      optionalUrl = Optional.of(record.get("url", String.class));
    }

    Optional<LocalDate> releaseYear = Optional.empty();
    if (record.get("release_year", Integer.class) != null) {
      LocalDate releaseDate = LocalDate.of(record.get("release_year", Integer.class), 1, 2);
      releaseYear = Optional.of(releaseDate);
    }

    Optional<LocalDate> endYear = Optional.empty();
    if (record.get("end_year", Integer.class) != null) {
      LocalDate endDate = LocalDate.of(record.get("end_year", Integer.class), 1, 2);
      endYear = Optional.of(endDate);
    }

    JSONB tagsJSONB = record.get("tags", JSONB.class);
    Optional<Map<String, String>> tags = Optional.empty();
    if (tagsJSONB != null && !tagsJSONB.data().isBlank()) {
      tags = MediaHelper.translateTags(tagsJSONB);
    }

    return new Series(id, title, genre, seasons, platform, optionalUrl, releaseYear, endYear, tags);
  }

  @Override
  public Optional<Series> findById(int id) {
    SeriesRecord seriesRecord =
        create.selectFrom(Tables.SERIES).where(Tables.SERIES.ID.eq(id)).fetchOne();
    if (seriesRecord != null) {
      return Optional.of(translateRecordToSeries(seriesRecord));
    }
    return Optional.empty();
  }

  private Series translateRecordToSeries(SeriesRecord seriesRecord) {
    Set<String> genre = Arrays.stream(seriesRecord.getGenre()).collect(Collectors.toSet());
    Map<String, Integer> seasons = new HashMap<>();
    String jsonData = seriesRecord.getSeasons().data();
    // TODO: check if this is correct
    if (!jsonData.isBlank()) {
      seasons = MediaHelper.translateJsonToSeasons(jsonData);
    }

    Optional<String> optionalUrl = Optional.empty();
    if (seriesRecord.getUrl() != null && !seriesRecord.getUrl().isBlank()) {
      optionalUrl = Optional.of(seriesRecord.getUrl());
    }

    Optional<LocalDate> releaseYear = Optional.empty();
    if (seriesRecord.getReleaseYear() != null) {
      LocalDate releaseDate = LocalDate.of(seriesRecord.getReleaseYear(), 1, 2);
      releaseYear = Optional.of(releaseDate);
    }

    Optional<LocalDate> endYear = Optional.empty();
    if (seriesRecord.getEndYear() != null) {
      LocalDate endDate = LocalDate.of(seriesRecord.getEndYear(), 1, 2);
      endYear = Optional.of(endDate);
    }

    return new Series(
        seriesRecord.getId(),
        seriesRecord.getTitle(),
        genre,
        seasons,
        seriesRecord.getPlatform(),
        optionalUrl,
        releaseYear,
        endYear,
        MediaHelper.translateTags(seriesRecord.getTags()));
  }
}
