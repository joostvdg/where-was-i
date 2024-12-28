package net.joostvdg.wwi.tracking;

import net.joostvdg.wwi.media.*;
import net.joostvdg.wwi.user.User;

import java.util.Set;

public interface ProgressService {
    Set<Progress> getProgressForUserAndMedia(int userId, Set<Media> items);

    void createMovieProgressForUser(User user, Movie movie);

    void createSeriesProgressForUser(User user, Series series);

    void createVideoGameProgressForUser(User user, VideoGame videoGame);

    void updateProgress(Progress updatedProgress);
}
