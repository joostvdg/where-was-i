/* (C)2024 */
package net.joostvdg.wwi.user.internal;

import com.vaadin.flow.server.VaadinSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import net.joostvdg.wwi.model.wwi_auth.Tables;
import net.joostvdg.wwi.model.wwi_auth.tables.records.UsersRecord;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class UserServiceImpl implements UserService {

  private final DSLContext create;

  private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  public UserServiceImpl(DSLContext create) {
    this.create = create;
  }

  @Override
  public User getLoggedInUser() {

    if (VaadinSession.getCurrent().getAttribute("user") != null) {
      return (User) VaadinSession.getCurrent().getAttribute("user");
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = "Unknown";
    String externalId = "Unknown";
    String name = "Unknown";
    String email = "Unknown";
    String accountType = "Unknown";

    logger.info("Authentication: {}", authentication.getClass().getName());
    switch (authentication.getClass().getName()) {
      case "org.springframework.security.authentication.UsernamePasswordAuthenticationToken":
        logger.info("Principal is UsernamePasswordAuthenticationToken");
        username = authentication.getName();
        externalId = username;
        accountType = "Local";
        logger.info("Principal: {}", authentication.getPrincipal().getClass().getName());
        if (authentication.getPrincipal() instanceof InetOrgPerson person) {
          logger.info("InetOrgPerson: {}", person);
          name = person.getGivenName();
          email = person.getMail();
          accountType = "LDAP";
        }

        break;
      case "org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken":
        logger.info("Principal is OAuth2AuthenticationToken");
        OAuth2AuthenticatedPrincipal principal =
            (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
        username = principal.getAttribute("name");
        int idInt = 0;
        if (principal.getAttribute("id") != null) {
          idInt = principal.getAttribute("id");
        }
        externalId = String.valueOf(idInt);
        // if not, create a new user
        name = principal.getAttribute("name");
        email = principal.getAttribute("email");
        accountType = "GitHub";
        break;
      default:
        logger.warn("Principal is unknown");
    }

    User user = findOrCreateUser(username, externalId, accountType, name, email);
    VaadinSession.getCurrent().setAttribute("user", user);
    return user;
  }

  private User findOrCreateUser(
      String username, String externalId, String accountType, String name, String email) {
    // Find users by username through the database
    Result<UsersRecord> result =
        create.selectFrom(Tables.USERS).where(Tables.USERS.USERNAME.eq(username)).fetch();
    if (result.isEmpty()) {
      // Create a new user
      logger.info("Creating new user: {}", username);
      var now = LocalDateTime.now(ZoneId.of("UTC"));
      create
          .insertInto(Tables.USERS)
          .set(Tables.USERS.ACCOUNT_NUMBER, externalId)
          .set(Tables.USERS.ACCOUNT_TYPE, accountType)
          .set(Tables.USERS.USERNAME, username)
          .set(Tables.USERS.NAME, name)
          .set(Tables.USERS.EMAIL, email)
          .set(Tables.USERS.DATE_JOINED, now)
          .set(Tables.USERS.DATE_LAST_LOGIN, now)
          .execute();

      // TODO: can we do this in one go?
      UsersRecord newUserRecord =
          create.selectFrom(Tables.USERS).where(Tables.USERS.USERNAME.eq(username)).fetchOne();
      if (newUserRecord == null) {
        throw new IllegalStateException("User not found after creation");
      }
      return translateRecordToUser(newUserRecord);
    } else {
      logger.info("Found user: {}", result.getFirst());
      UsersRecord existingUser = result.getFirst();
      var now = LocalDateTime.now(ZoneId.of("UTC"));
      // update last login
      create
          .update(Tables.USERS)
          .set(Tables.USERS.DATE_LAST_LOGIN, now)
          .where(Tables.USERS.ID.eq(existingUser.getId()))
          .execute();
      existingUser.setDateLastLogin(now);
      return translateRecordToUser(existingUser);
    }
  }

  @Override
  public List<User> getAllUsers() {
    Result<UsersRecord> result = create.selectFrom(Tables.USERS).fetch();
    List<User> users = new ArrayList<>();
    for (UsersRecord usersRecord : result) {
      User user = translateRecordToUser(usersRecord);
      users.add(user);
    }
    return users;
  }

  private User translateRecordToUser(UsersRecord usersRecord) {
    return new User(
        usersRecord.getId(),
        usersRecord.getAccountNumber(),
        usersRecord.getAccountType(),
        usersRecord.getUsername(),
        usersRecord.getName(),
        usersRecord.getEmail(),
        usersRecord.getDateJoined().toLocalDate(),
        usersRecord.getDateLastLogin() != null
            ? usersRecord.getDateLastLogin().toLocalDate()
            : null);
  }

  @Override
  public boolean userExists(User user) {
    return getUserForUsername(user.username()).isPresent();
  }

  @Override
  public Optional<User> getUserForUsername(String username) {
    List<User> usersFound = new ArrayList<>();
    create
        .selectFrom(Tables.USERS)
        .where(Tables.USERS.USERNAME.eq(username))
        .fetch()
        .forEach(
            rec -> {
              User user = translateRecordToUser(rec);
              usersFound.add(user);
            });

    if (usersFound.isEmpty()) {
      return Optional.empty();
    }

    // there should be only one
    if (usersFound.size() > 1) {
      throw new IllegalStateException("Multiple users found for username: " + username);
    }

    return Optional.of(usersFound.getFirst());
  }

  @Override
  public User translateViewRecordToUser(
      Record watchlistUserViewRecord, String userDataPrefix, boolean fullUser) {
    if (watchlistUserViewRecord.get(userDataPrefix + "_id") == null) {
      return null;
    }
    int userId = watchlistUserViewRecord.get(userDataPrefix + "_id", Integer.class);
    String name = watchlistUserViewRecord.get(userDataPrefix + "_name", String.class);
    String email = watchlistUserViewRecord.get(userDataPrefix + "_email", String.class);

    LocalDate dateJoined = null;
    LocalDate dateLastLogin = null;
    String accountNumber = "";
    String accountType = "";

    // The Read and Write shared users only have the id, name, and email in the View
    if (fullUser) {
      accountNumber = watchlistUserViewRecord.get(userDataPrefix + "_account_number", String.class);
      accountType = watchlistUserViewRecord.get(userDataPrefix + "_account_type", String.class);
      LocalDateTime dateTimeJoined =
          watchlistUserViewRecord.get(userDataPrefix + "_date_joined", LocalDateTime.class);
      LocalDateTime dateTimeLastLogin =
          watchlistUserViewRecord.get(userDataPrefix + "_date_last_login", LocalDateTime.class);
      if (dateTimeJoined != null) {
        dateJoined = dateTimeJoined.toLocalDate();
      }

      if (dateTimeLastLogin != null) {
        dateLastLogin = dateTimeLastLogin.toLocalDate();
      }
    }

    return new User(
        userId, accountNumber, accountType, userDataPrefix, name, email, dateJoined, dateLastLogin);
  }

  @Override
  public User getUserById(int userId) {
    UsersRecord usersRecord = create.fetchOne(Tables.USERS, Tables.USERS.ID.eq(userId));
    if (usersRecord == null) {
      return null;
    }
    return translateRecordToUser(usersRecord);
  }
}
