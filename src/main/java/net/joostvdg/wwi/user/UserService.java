package net.joostvdg.wwi.user;


import org.jooq.Record;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface UserService {
    User getLoggedInUser();

    List<User> getAllUsers();

    boolean userExists(User selectedUser);

    Optional<User> getUserForUsername(String username);

    User translateViewRecordToUser(Record watchlistUserViewRecord, String userDataPrefix);
}
