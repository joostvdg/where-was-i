package net.joostvdg.wwi.user.impl;

import net.joostvdg.wwi.media.*;
import net.joostvdg.wwi.tracking.WatchList;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserServiceImpl implements UserService {

    private final Set<User> users;
    // create lock for users
    private final Object lock = new Object();

    private final AtomicInteger progressIdCounter = new AtomicInteger(1);
    private final AtomicInteger userIdCounter = new AtomicInteger(1);

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl() {
        this.users = new HashSet<>();
    }


    // TODO: Implement this using the database
    @Override
    public User getLoggedInUser() {

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
                logger.info("Principal: {}" , authentication.getPrincipal().getClass().getName());
                if (authentication.getPrincipal() instanceof InetOrgPerson person) {
                    logger.info("InetOrgPerson: {}", person);
                    name = person.getGivenName();
                    email = person.getMail();
                    accountType = "LDAP";
                }

                break;
            case "org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken":
                logger.info("Principal is OAuth2AuthenticationToken");
                OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
                username = principal.getAttribute("name");
                int idInt = 0;
                if (principal.getAttribute("id") != null) {
                    idInt = principal.getAttribute("id");
                }
                externalId = String.valueOf(idInt);
                // if not, create a new user
                name = principal.getAttribute("name");
                email = principal.getAttribute("email");
                accountType = "GitHub"; // TODO: make this dynamic
                break;
            default:
                logger.warn("Principal is unknown");
        }

        for (User user : users) {
            if (user.username().equals(username)) { // TODO: should this be go via ID when we have a database?
                return user;
            }
        }

        User user = new User(userIdCounter.getAndIncrement(), externalId, accountType, username, name, email, LocalDate.now(), LocalDate.now(), new HashSet<>());
        users.add(user);
        return user;
    }


    @Override
    public void updateProgress(User user, Progress progress) {
        // verify user exists
        if (users.contains(user)) {
            synchronized (lock) {
                // replace the user, as it is immutable
                users.remove(user);
                Set<Progress> progresses = new HashSet<>(user.progress());
                // find existing progress by ID
                progresses.removeIf(p -> p.getId() == progress.getId());
                progresses.add(progress);
                User updatedUser = cloneUserWithUpdatedProgress(user, progresses);
                users.add(updatedUser);
                lock.notifyAll();
            }
        }
    }


    @Override
    public void addProgress(User user, Progress progress) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (progress == null) {
            throw new IllegalArgumentException("SeriesProgress cannot be null");
        }

        if (progress.getMedia() == null) {
            throw new IllegalArgumentException("Media cannot be null");
        }

        if (progress.getId() > 0 && containsProgress(user, progress)) {
            throw new IllegalArgumentException("SeriesProgress already exists");
        }

        Progress progressWithId = switch (progress) {
            case SeriesProgress seriesProgress ->
                    new SeriesProgress(progressIdCounter.getAndIncrement(), seriesProgress.finished(), seriesProgress.getMedia(), seriesProgress.getProgress(), seriesProgress.favorite());
            case VideoGameProgress videoGameProgress ->
                    new VideoGameProgress(progressIdCounter.getAndIncrement(), videoGameProgress.getProgress(), videoGameProgress.finished(), (VideoGame) videoGameProgress.getMedia(), videoGameProgress.favorite());
            case MovieProgress movieProgress ->
                    new MovieProgress(progressIdCounter.getAndIncrement(), movieProgress.getMedia(), movieProgress.getProgress(), movieProgress.favorite(), movieProgress.finished());
            default -> throw new IllegalArgumentException("Unsupported progress type");
        };

        // TODO: replace when using a database
        if (userExists(user)) {
            synchronized (lock) {
                // replace the user, as it is immutable

                Set<Progress> progresses = new HashSet<>(user.progress());
                progresses.add(progressWithId);
                User user1 = cloneUserWithUpdatedProgress(user, progresses);
                logger.info("User has the following progresses: {}", user1.progress());
                var userToRemove = users.stream().filter(u -> u.username().equals(user.username())).findFirst().orElseThrow(() -> new IllegalArgumentException("User does not exist"));
                users.remove(userToRemove);
                users.add(user1);
                logger.info("Added progress to user: {}", user1);
                lock.notifyAll();
            }
        }
    }

    @Override
    public List<User> getAllUsers() {
        return Collections.unmodifiableList(List.copyOf(users));
    }

    private boolean containsProgress(User user, Progress newProgress) {
        for (Progress progress : user.progress()) {
            if (progress.getId() == newProgress.getId() || progress.getMedia().getId() == newProgress.getMedia().getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean userExists(User user) {
        // TODO: validate on User ID
        // for now, we limit the check to username, which should be unique as well
        var foundUser = users.stream().filter(u -> u.username().equals(user.username())).findFirst().orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        return foundUser != null;
    }

    @Override
    public Optional<User> getUserForUsername(String username) {
        for (User user : users) {
            if (user.username().equals(username)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    private User cloneUserWithUpdatedProgress(User user, Set<Progress> progresses) {
        return new User(user.id(), user.accountNumber(), user.accountType(), user.username(), user.name(), user.email(), user.dateJoined(), user.dateLastLogin(), progresses);
    }

}
