package net.joostvdg.wwi.user.impl;

import net.joostvdg.wwi.media.*;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserServiceImpl implements UserService {

    private final Set<User> users;
    // create lock for users
    private final Object lock = new Object();

    private final AtomicInteger progressIdCounter = new AtomicInteger(1);

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl() {
        this.users = new HashSet<>();
        initUsers();
    }

    private User createDummyUser( Set<Progress> progress) {
        String accountNumber = "1234567890";
        String accountType = "Premium";
        String username = "johndoe";
        String email = "johndoe@example.com";
        LocalDate dateJoined = LocalDate.of(2020, 5, 15);
        LocalDate dateLastLogin = LocalDate.of(2024, 8, 10);
        return new User(1L, accountNumber, accountType, username, username, email, dateJoined, dateLastLogin, progress);
    }
    
    private void initUsers() {
        User user1 = createDummyUser(new HashSet<>());
        this.users.add(user1);
    }


    // TODO: Implement this using the database
    @Override
    public User getLoggedInUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
        String username = principal.getAttribute("login");

        for (User user : users) {
            if (user.username().equals(username)) {
                return user;
            }
        }

        int idInt = 0;
        if (principal.getAttribute("id") != null) {
            idInt = principal.getAttribute("id");
        }
        long id = idInt;

        // see if we already have a user with this id
        for (User user : users) {
            if (user.id() == id) {
                return user;
            }
        }
        // if not, create a new user
        String name = principal.getAttribute("name");
        String email = principal.getAttribute("email");

        User user = new User(100, String.valueOf(idInt), "GitHub", username, name, email, LocalDate.now(), LocalDate.now(), Collections.emptySet());
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
                Set<Progress> progresses = user.progress();
                // find existing progress by ID
                progresses.removeIf(p -> p.getId() == progress.getId());
                progresses.add(progress);
                User user1 = createDummyUser(progresses);
                users.add(user1);
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


        if (progress.getId() > 0 && user.progress().contains(progress.getId() )) {
            throw new IllegalArgumentException("SeriesProgress already exists");
        }

        Progress progressWithId = null;
        switch (progress) {
            case SeriesProgress seriesProgress:
                progressWithId = new SeriesProgress(progressIdCounter.getAndIncrement(), seriesProgress.finished(), seriesProgress.getMedia(), seriesProgress.getProgress(), seriesProgress.favorite());
                break;
            case VideoGameProgress videoGameProgress:
                progressWithId = new VideoGameProgress(progressIdCounter.getAndIncrement(), videoGameProgress.getProgress(), videoGameProgress.finished(), (VideoGame) videoGameProgress.getMedia(), videoGameProgress.favorite());
                break;
            case MovieProgress movieProgress:
                progressWithId = new MovieProgress(progressIdCounter.getAndIncrement(), movieProgress.getMedia(), movieProgress.getProgress(), movieProgress.favorite(), movieProgress.finished());
                break;
            default:
                throw new IllegalArgumentException("Unsupported progress type");
        }
        // create new progress with id from idCounter

        // TODO: replace when using a database
        if (userExists(user)) {
            synchronized (lock) {
                // replace the user, as it is immutable
                users.remove(user);
                Set<Progress> progresses = new HashSet<>(user.progress());
                progresses.add(progressWithId);
                User user1 = cloneUserWithUpdatedProgress(user, progresses);
                logger.info("User has the following progresses: {}", user1.progress());
                users.add(user1);
                logger.info("Added progress to user: {}", user1);
                lock.notifyAll();
            }
        }
    }

    private boolean userExists(User user) {
        // TODO: validate on User ID
        // for now, we limit the check to username, which should be unique as well
        var foundUser = users.stream().filter(u -> u.username().equals(user.username())).findFirst().orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        return foundUser != null;
    }

    private User cloneUserWithUpdatedProgress(User user, Set<Progress> progresses) {
        return new User(user.id(), user.accountNumber(), user.accountType(), user.username(), user.name(), user.email(), user.dateJoined(), user.dateLastLogin(), progresses);
    }
}
