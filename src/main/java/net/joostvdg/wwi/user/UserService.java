/* (C)2024 */
package net.joostvdg.wwi.user;

import java.util.List;
import java.util.Optional;
import org.jooq.Record;
import org.springframework.stereotype.Component;

@Component
public interface UserService {
  User getLoggedInUser();

  List<User> getAllUsers();

  boolean userExists(User selectedUser);

  Optional<User> getUserForUsername(String username);

  User translateViewRecordToUser(Record watchlistUserViewRecord, String userDataPrefix, boolean fullUserData);

  User getUserById(int userId);
}
