package net.joostvdg.wwi.user.impl;

import net.joostvdg.wwi.media.*;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;
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
        return new User(1L, accountNumber, accountType, username, email, dateJoined, dateLastLogin, progress);
    }
    
    private void initUsers() {
        User user1 = createDummyUser(new HashSet<>());
        this.users.add(user1);
    }


    // TODO: Implement this using the database
    @Override
    public User getLoggedInUser() {
        return this.users.stream().findFirst().orElse(null);
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
        if (users.contains(user)) {
            synchronized (lock) {
                // replace the user, as it is immutable
                users.remove(user);
                Set<Progress> progresses = user.progress();
                progresses.add(progressWithId);
                User user1 = createDummyUser(progresses);
                users.add(user1);
                lock.notifyAll();
            }
        }
    }
}
