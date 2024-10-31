package net.joostvdg.wwi.media;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.provider.ListDataProvider;
import jakarta.annotation.security.PermitAll;
import net.joostvdg.wwi.main.MainView;
import net.joostvdg.wwi.main.ViewNotifications;
import net.joostvdg.wwi.tracking.WatchList;
import net.joostvdg.wwi.tracking.WatchlistService;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "movies", layout = MainView.class)
@PageTitle("Movies | Where Was I?")
@PermitAll // TODO: limit access to Admin users?
public class MovieListView extends VerticalLayout {

    private Grid<Movie> movieGrid;
    private ListDataProvider<Movie> dataProvider;
    private TextField titleFilter;
    private TextField platformFilter;
    private ComboBox<Integer> releaseYearFilter;
    private ComboBox<String> genreFilter;

    private final MovieService movieService;
    private final WatchlistService watchlistService;
    private final UserService userService;

    public MovieListView(MovieService movieService, WatchlistService watchlistService, UserService userService) {
        this.movieService = movieService;
        this.watchlistService = watchlistService;
        this.userService = userService;
        // Fetch all movies from the service (or repository)
        List<Movie> movieList = movieService.findAll();
        dataProvider = new ListDataProvider<>(movieList);

        // Initialize Grid
        movieGrid = new Grid<>(Movie.class);
        movieGrid.setItems(dataProvider);
        configureGridColumns();

        // Create search fields
        titleFilter = new TextField("Title");
        platformFilter = new TextField("Platform");
        releaseYearFilter = new ComboBox<>("Release Year");
        releaseYearFilter.setItems(getReleaseYears(movieList));
        genreFilter = new ComboBox<>("Genre");
        genreFilter.setItems(getAllGenres(movieList));

        // Add change listeners for filtering
        titleFilter.addValueChangeListener(e -> applyFilters());
        platformFilter.addValueChangeListener(e -> applyFilters());
        releaseYearFilter.addValueChangeListener(e -> applyFilters());
        genreFilter.addValueChangeListener(e -> applyFilters());

        // Layout for search filters
        HorizontalLayout filterLayout = new HorizontalLayout(titleFilter, platformFilter, releaseYearFilter, genreFilter);
        add(filterLayout, movieGrid);

        // Create "New Movie" button
        Button newMovieButton = new Button("New Movie", e -> openMovieCreationDialog());
        add(newMovieButton);
    }

    private void configureGridColumns() {
        // Configure the columns for the movie grid
        movieGrid.removeAllColumns();
        // Add "Action" column with buttons for View, Edit, and Add to Watchlist
        movieGrid.addColumn(new ComponentRenderer<>(movie -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();

            // View Button
            Icon viewIcon = new Icon(VaadinIcon.EYE);
            viewIcon.getStyle().set("cursor", "pointer");
            viewIcon.setColor("white");
            viewIcon.addClickListener(e -> openMovieDetailsDialog(movie));

            // Edit Button
            Icon editIcon = new Icon(VaadinIcon.EDIT);
            editIcon.getStyle().set("cursor", "pointer");
            editIcon.setColor("red");
            editIcon.addClickListener(e -> openMovieEditDialog(movie));

            // Add to Watchlist Button
            Icon addToWatchlistIcon = new Icon(VaadinIcon.LIST);
            addToWatchlistIcon.getStyle().set("cursor", "pointer");
            addToWatchlistIcon.setColor("green");
            addToWatchlistIcon.addClickListener(e -> openAddToWatchlistDialog(movie));

            actionsLayout.add(viewIcon, editIcon, addToWatchlistIcon);
            return actionsLayout;
        })).setHeader("Actions");
        movieGrid.addColumn(Movie::title).setHeader("Title");
        movieGrid.addColumn(Movie::platform).setHeader("Platform");
        movieGrid.addColumn(Movie::director).setHeader("Director");
        movieGrid.addColumn(movie -> movie.durationInMinutes() + " minutes").setHeader("Duration");
        movieGrid.addColumn(movie -> movie.releaseYear()).setHeader("Release Year");
        movieGrid.addColumn(movie -> String.join(", ", movie.genre())).setHeader("Genres");
        movieGrid.addColumn(movie -> movie.url().orElse("")).setHeader("URL");
        movieGrid.addColumn(movie -> movie.tags().map(tags -> tags.toString()).orElse("No tags")).setHeader("Tags");
    }

    private void openMovieDetailsDialog(Movie movie) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Display movie details
        formLayout.addFormItem(new TextField("Title", movie.title()), "Title");
        formLayout.addFormItem(new TextField("Platform", movie.platform()), "Platform");
        formLayout.addFormItem(new TextField("Director", movie.director()), "Director");
        formLayout.addFormItem(new TextField("Duration", movie.durationInMinutes() + " minutes"), "Duration");
        formLayout.addFormItem(new TextField("Release Year", String.valueOf(movie.releaseYear())), "Release Year");
        formLayout.addFormItem(new TextField("Genres", String.join(", ", movie.genre())), "Genres");
        formLayout.addFormItem(new TextField("URL", movie.url().orElse("")), "URL");
        formLayout.addFormItem(new TextField("Tags", movie.tags().map(tags -> tags.toString()).orElse("No tags")), "Tags");

        // All fields should be read-only
        formLayout.getChildren().filter( child -> child instanceof TextField)
                .map(child -> (TextField) child)
                .forEach(field -> field.setReadOnly(true));

        Button closeButton = new Button("Close", e -> dialog.close());
        dialog.add(formLayout, closeButton);
        dialog.open();
    }

    private void openMovieEditDialog(Movie movie) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Fields for Movie editing
        TextField titleField = new TextField("Title", movie.title());
        TextField platformField = new TextField("Platform", movie.platform());
        TextField directorField = new TextField("Director", movie.director());
        TextField durationField = new TextField("Duration (in minutes)", String.valueOf(movie.durationInMinutes()));
        TextField releaseYearField = new TextField("Release Year", String.valueOf(movie.releaseYear()));
        TextField genreField = new TextField("Genres (comma-separated)", String.join(", ", movie.genre()));
        TextField urlField = new TextField("URL (optional)", movie.url().orElse(""));

        // Save button to handle the Movie edit
        Button saveButton = new Button("Save", event -> {
            try {
                // Update movie object with edited values
                movieGrid.getDataProvider().refreshItem(movie);
                Notification.show("Movie updated: " + titleField.getValue());
                dialog.close();
            } catch (NumberFormatException e) {
                Notification.show("Invalid input for duration or release year.", 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        // Add components to the form layout
        formLayout.add(titleField, platformField, directorField, durationField, releaseYearField, genreField, urlField);
        dialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }



    private void openAddToWatchlistDialog(Movie movie) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Fetch available watchlists (you may have a service to get this)
        var user = userService.getLoggedInUser();
        List<WatchList> availableWatchlists = watchlistService.getWatchListsForUser(user);

        // Dropdown to select a watchlist
        ComboBox<WatchList> watchlistComboBox = new ComboBox<>("Select Watchlist");
        watchlistComboBox.setItems(availableWatchlists);
        watchlistComboBox.setItemLabelGenerator(WatchList::getName);  // Assuming Watchlist has getName() method

        // Add button to add movie to selected watchlist
        Button addToWatchlistButton = new Button("Add", event -> {
            WatchList selectedWatchlist = watchlistComboBox.getValue();
            if (selectedWatchlist == null) {
                ViewNotifications.showErrorNotification("Please select a watchlist");
                return;
            }

            if (selectedWatchlist.getItems().contains(movie)) {
                ViewNotifications.showErrorNotification("Movie already exists in watchlist: " + selectedWatchlist.getName());
                dialog.close();
                return;
            }

            selectedWatchlist.getItems().add(movie);
            createMovieProgressForUser(user, movie);
            ViewNotifications.showSuccessNotification("Movie added to watchlist: " + selectedWatchlist.getName());
            dialog.close();
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        formLayout.add(watchlistComboBox);
        dialog.add(formLayout, new HorizontalLayout(addToWatchlistButton, cancelButton));
        dialog.open();
    }

    private void createMovieProgressForUser(User user, Movie movie) {
        MovieProgress movieProgress = new MovieProgress(0, movie, Map.of("Minutes Watched", 0), false, false);
        userService.addProgress(user, movieProgress);
    }


    private void applyFilters() {
        dataProvider.clearFilters();

        // Apply title filter
        if (!titleFilter.isEmpty()) {
            dataProvider.addFilter(movie -> movie.title().toLowerCase().contains(titleFilter.getValue().toLowerCase()));
        }

        // Apply platform filter
        if (!platformFilter.isEmpty()) {
            dataProvider.addFilter(movie -> movie.platform().toLowerCase().contains(platformFilter.getValue().toLowerCase()));
        }

        // Apply release year filter
        if (releaseYearFilter.getValue() != null) {
            dataProvider.addFilter(movie -> movie.releaseYear() == releaseYearFilter.getValue());
        }

        // Apply genre filter
        if (genreFilter.getValue() != null) {
            dataProvider.addFilter(movie -> movie.genre().contains(genreFilter.getValue()));
        }
    }

    private Set<Integer> getReleaseYears(List<Movie> movieList) {
        return movieList.stream().map(Movie::releaseYear).collect(Collectors.toSet());
    }

    private Set<String> getAllGenres(List<Movie> movieList) {
        return movieList.stream().flatMap(movie -> movie.genre().stream()).collect(Collectors.toSet());
    }

    private void repoplateList(Movie newMovie) {
        var movieList = dataProvider.getItems();
        movieList.add(newMovie);
        dataProvider.refreshAll();
        releaseYearFilter.setItems(getReleaseYears((List<Movie>) movieList));
        genreFilter.setItems(getAllGenres((List<Movie>) movieList));
    }

    private void openMovieCreationDialog() {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Fields for Movie creation
        TextField titleField = new TextField("Title");
        TextField platformField = new TextField("Platform");
        TextField directorField = new TextField("Director");
        TextField durationField = new TextField("Duration (in minutes)");
        TextField releaseYearField = new TextField("Release Year");
        TextField genreField = new TextField("Genres (comma-separated)");
        TextField urlField = new TextField("URL (optional)");

        // Save button
        Button saveButton = new Button("Save", event -> {
            try {
                // Parse input values
                String title = titleField.getValue();
                String platform = platformField.getValue();
                String director = directorField.getValue();
                int duration = Integer.parseInt(durationField.getValue());
                int releaseYear = Integer.parseInt(releaseYearField.getValue());
                Set<String> genres = new HashSet<>(Arrays.asList(genreField.getValue().split(",\\s*")));
                Optional<String> url = urlField.isEmpty() ? Optional.empty() : Optional.of(urlField.getValue());

                // Create new Movie object
                Movie newMovie = new Movie(0L,title, platform, director, duration, releaseYear, genres, url, Optional.empty());

                // Save movie using service and update grid
                newMovie = movieService.save(newMovie);
                repoplateList(newMovie);
                ViewNotifications.showSuccessNotification("New movie added: " + title);

                dialog.close();
            } catch (NumberFormatException e) {
                ViewNotifications.showErrorNotification("Invalid input for duration or release year.");
            }
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        // Add components to the form
        formLayout.add(titleField, platformField, directorField, durationField, releaseYearField, genreField, urlField);
        dialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));

        dialog.open();
    }
}
