package net.joostvdg.wwi;


import net.joostvdg.wwi.media.*;
import net.joostvdg.wwi.model.tables.records.MoviesRecord;
import net.joostvdg.wwi.model.tables.records.SeriesRecord;
import net.joostvdg.wwi.model.tables.records.UsersRecord;
import net.joostvdg.wwi.model.tables.records.VideoGamesRecord;
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
        MoviesRecord.class,
        SeriesRecord.class,
        VideoGamesRecord.class,
        Movie.class,
        MovieProgress.class,
        VideoGame.class,
        VideoGameProgress.class,
        Series.class,
        SeriesProgress.class,
        WatchList.class,
})
@ImportRuntimeHints(ReflectionConfiguration.AppRuntimeHintsRegistrar.class)
public class ReflectionConfiguration {
    public static class AppRuntimeHintsRegistrar implements RuntimeHintsRegistrar     {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection()
                    .registerType(UsersRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(MoviesRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(SeriesRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(VideoGamesRecord.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(Movie.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(MovieProgress.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(VideoGame.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(VideoGameProgress.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(Series.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(SeriesProgress.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
                    .registerType(WatchList.class, PUBLIC_FIELDS, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)
            ;
        }
    }
}