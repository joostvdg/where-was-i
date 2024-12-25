package net.joostvdg.wwi.user;

import net.joostvdg.wwi.media.Progress;
import org.jooq.Record;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface UserService {
    User getLoggedInUser();

    void updateProgress(User user, Progress progress);

    void addProgress(User user, Progress progress);

    List<User> getAllUsers();

    boolean userExists(User selectedUser);

    Optional<User> getUserForUsername(String username);

    User translateViewRecordToUser(Record watchlistUserViewRecord, String userDataPrefix);
}
