package net.joostvdg.wwi;


import net.joostvdg.wwi.media.*;
import net.joostvdg.wwi.model.tables.records.*;
import net.joostvdg.wwi.tracking.WatchList;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS;
import static org.springframework.aot.hint.MemberCategory.PUBLIC_FIELDS;

@Configuration
@RegisterReflectionForBinding({
        UsersRecord.class,
        Movie.class,
        MoviesRecord.class,
        MovieProgress.class,
        MovieProgressRecord.class,
        VideoGame.class,
        VideoGamesRecord.class,
        VideoGameProgress.class,
        VideoGameProgressRecord.class,
        Series.class,
        SeriesRecord.class,
        SeriesProgress.class,
        SeriesProgressRecord.class,
        WatchList.class,
        WatchListRecord.class,
        WatchListReadSharedRecord.class,
        WatchListWriteSharedRecord.class,
        WatchListWithUsersRecord.class,
        WatchListWithMediaRecord.class,
        WatchListMoviesRecord.class,
        WatchListSeriesRecord.class,
        WatchListVideoGamesRecord.class,
        AllProgressRecord.class,
        org.jooq.Record.class,
        org.jooq.Result.class
})
@ImportRuntimeHints(ReflectionConfiguration.AppRuntimeHintsRegistrar.class)
public class ReflectionConfiguration {
    public static class AppRuntimeHintsRegistrar implements RuntimeHintsRegistrar     {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection()
                    .registerType(UsersRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(Movie.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(MoviesRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(MovieProgress.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(MovieProgressRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(VideoGame.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(VideoGamesRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(VideoGameProgress.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(VideoGameProgressRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(Series.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(SeriesRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(SeriesProgress.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(SeriesProgressRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(WatchList.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(WatchListRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(WatchListReadSharedRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(WatchListWriteSharedRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(WatchListWithUsersRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(WatchListWithMediaRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(WatchListMoviesRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(WatchListSeriesRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(WatchListVideoGamesRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(AllProgressRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(org.jooq.Record.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(org.jooq.Result.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
            ;
        }
    }
}