/* (C)2024 */
package net.joostvdg.wwi.progress;

import java.util.Set;
import net.joostvdg.wwi.media.*;
import net.joostvdg.wwi.media.Movie;
import net.joostvdg.wwi.media.Series;
import net.joostvdg.wwi.media.VideoGame;
import net.joostvdg.wwi.user.User;

public interface ProgressService {
  Set<Progress> getProgressForUserAndMedia(int userId, Set<Media> items);

  void createMovieProgressForUser(User user, Movie movie);

  void createSeriesProgressForUser(User user, Series series);

  void createVideoGameProgressForUser(User user, VideoGame videoGame);

  void updateProgress(Progress updatedProgress);
}
