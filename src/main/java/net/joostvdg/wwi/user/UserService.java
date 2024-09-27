package net.joostvdg.wwi.user;

import net.joostvdg.wwi.media.Progress;
import net.joostvdg.wwi.media.SeriesProgress;
import net.joostvdg.wwi.media.VideoGameProgress;
import org.springframework.stereotype.Component;

@Component
public interface UserService {
    User getLoggedInUser();

    void updateProgress(User user, Progress progress);

    void addProgress(User user, Progress progress);
}
