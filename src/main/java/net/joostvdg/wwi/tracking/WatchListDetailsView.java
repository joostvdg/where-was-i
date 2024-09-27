package net.joostvdg.wwi.tracking;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import net.joostvdg.wwi.main.MainView;
import net.joostvdg.wwi.media.*;
import com.vaadin.flow.component.textfield.TextField;
import net.joostvdg.wwi.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "watchlist-details", layout = MainView.class)
@PageTitle("Watchlist Details | Where Was I?")
public class WatchListDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    public static final String FINISHED = "Finished";
    public static final String FAVORITE = "Favorite";
    public static final String NOT_FAVORITE = "Not Favorite";
    public static final String ALL = "All";
    public static final String NOT_FINISHED = "Not Finished";
    public static final String TITLE = "Title";
    public static final String PLATFORM = "Platform";
    public static final String PROGRESS = "Progress";
    private final WatchlistService watchlistService;

    private final SeriesService seriesService;
    private final UserService userService;
    private final VideoGameService videoGameService;
    private final MovieService movieService;

    private final Logger logger = LoggerFactory.getLogger(WatchListDetailsView.class);

    private WatchList currentWatchList;
    private ListDataProvider<Progress> dataProvider;
    private final FormLayout watchListDetailsForm = new FormLayout(); // Section 1: Watchlist properties
    private final Grid<Progress> progressGrid = new Grid<>(Progress.class, false);

    public WatchListDetailsView(WatchlistService watchlistService, SeriesService seriesService, UserService userService, VideoGameService videoGameService, MovieService movieService) {
        this.watchlistService = watchlistService;
        this.seriesService = seriesService;
        this.videoGameService = videoGameService;
        this.movieService = movieService;

        // Add buttons above the media grid
        HorizontalLayout mediaButtons = new HorizontalLayout();
        Button addMovieButton = new Button("Add Movie", e -> openMovieDialog());
        Button addSeriesButton = new Button("Add Series", e -> openSeriesDialog());
        Button addVideoGameButton = new Button("Add Video Game", e -> openVideoGameDialog());

        mediaButtons.add(addMovieButton, addSeriesButton, addVideoGameButton);

        add(watchListDetailsForm, mediaButtons, progressGrid);
        this.userService = userService;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        // Find the WatchList by name (parameter), this is a simplified example
        // assume the parameter is the Long id of the WatchList
        logger.info("Parameter: " + parameter);

        // parse the parameter to a Long
        Long id = Long.parseLong(parameter);
        Optional<WatchList> watchList = watchlistService.getWatchlistById(id);

        watchList.ifPresentOrElse(this::showWatchListDetails,
                () -> watchListDetailsForm.add(new Span("WatchList not found")));
        watchList.ifPresent(value -> currentWatchList = value);


        configureProgressGrid();
    }

    private void showWatchListDetails(WatchList watchList) {
        // Section 1: Display WatchList properties in a FormLayout with read-only fields
        watchListDetailsForm.removeAll();

        TextField nameField = new TextField("Name");
        nameField.setValue(watchList.getName());
        nameField.setReadOnly(true);

        TextField descriptionField = new TextField("Description");
        descriptionField.setValue(watchList.getDescription());
        descriptionField.setReadOnly(true);

        TextField ownerField = new TextField("Owner");
        ownerField.setValue(watchList.getOwner().username());
        ownerField.setReadOnly(true);

        TextField favoriteField = new TextField(FAVORITE);
        favoriteField.setValue(watchList.isFavorite() ? "Yes" : "No");
        favoriteField.setReadOnly(true);

        TextField createdField = new TextField("Created");
        createdField.setValue(watchList.getCreated().toString());
        createdField.setReadOnly(true);

        TextField lastEditField = new TextField("Last Edit");
        lastEditField.setValue(watchList.getLastEdit().toString());
        lastEditField.setReadOnly(true);

        TextField readSharedField = new TextField("Read Shared With");
        readSharedField.setValue(watchList.getReadShared().size() + " users");
        readSharedField.setReadOnly(true);

        TextField writeSharedField = new TextField("Write Shared With");
        writeSharedField.setValue(watchList.getWriteShared().size() + " users");
        writeSharedField.setReadOnly(true);

        watchListDetailsForm.add(
                nameField,
                descriptionField,
                ownerField,
                favoriteField,
                createdField,
                lastEditField,
                readSharedField,
                writeSharedField
        );
    }

    private void configureProgressGrid() {
        // Add columns for the Media part
        // Add action icons (View and Edit) as the first column
        progressGrid.addColumn(new ComponentRenderer<>(progress -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();

            // View icon
            Icon viewIcon = new Icon(VaadinIcon.EYE);
            viewIcon.getStyle().set("cursor", "pointer");
            viewIcon.setColor("white");
            viewIcon.addClickListener(e -> openViewDialog(progress));

            // Edit icon
            Icon editIcon = new Icon(VaadinIcon.EDIT);
            editIcon.getStyle().set("cursor", "pointer");
            editIcon.setColor("red");
            editIcon.addClickListener(e -> openEditDialog(progress));

            actionsLayout.add(viewIcon, editIcon);
            return actionsLayout;
        })).setHeader("Actions").setAutoWidth(true);

        progressGrid.addColumn(progress -> progress.getMedia().getTitle())
                .setHeader(TITLE)
                .setSortable(true)
                .setAutoWidth(true);

        progressGrid.addColumn(progress -> progress.getMedia().getPlatform())
                .setHeader(PLATFORM)
                .setSortable(true)
                .setAutoWidth(true);

        // Add columns for the Progress part
        progressGrid.addColumn(progress -> progress.getProgress().toString())  // Display progress details (e.g., episodes watched)
                .setHeader(PROGRESS)
                .setAutoWidth(true);

        progressGrid.addColumn(progress -> progress.isFinished() ? "Yes" : "No")
                .setHeader(FINISHED)
                .setSortable(true)
                .setAutoWidth(true);

        progressGrid.addColumn(progress -> progress.isFavorite() ? "Yes" : "No")
                .setHeader(FAVORITE)
                .setSortable(true)
                .setAutoWidth(true);

        // Create the filter panel
        ComboBox<String> isFavoriteFilter = new ComboBox<>(FAVORITE);
        isFavoriteFilter.setItems(ALL, FAVORITE, NOT_FAVORITE);
        isFavoriteFilter.setValue(ALL);

        ComboBox<String> isFinishedFilter = new ComboBox<>(FINISHED);
        isFinishedFilter.setItems(ALL, FINISHED, NOT_FINISHED);
        isFinishedFilter.setValue(ALL);

        // Add listeners for the filters
        isFavoriteFilter.addValueChangeListener(event -> applyFilters(isFavoriteFilter, isFinishedFilter));
        isFinishedFilter.addValueChangeListener(event -> applyFilters(isFavoriteFilter, isFinishedFilter));

        // Layout for the filter panel
        HorizontalLayout filterPanel = new HorizontalLayout(isFavoriteFilter, isFinishedFilter);

        // Add the grid to the layout
        add(filterPanel, progressGrid);

        // Populate the grid with data
        populateProgressGrid(progressGrid);
    }

    private void applyFilters(ComboBox<String> isFavoriteFilter, ComboBox<String> isFinishedFilter) {
        dataProvider.clearFilters();

        // Apply favorite filter
        String favoriteValue = isFavoriteFilter.getValue();
        if (favoriteValue != null) {
            switch (favoriteValue) {
                case FAVORITE:
                    dataProvider.addFilter(Progress::isFavorite);
                    break;
                case NOT_FAVORITE:
                    dataProvider.addFilter(media -> !media.isFavorite());
                    break;
                case ALL:
                default:
                    // No filter is applied for "All"
                    break;
            }
        }

        String finishedValue = isFinishedFilter.getValue();
        if (finishedValue != null) {
            switch (finishedValue) {
                case FINISHED:
                    dataProvider.addFilter(Progress::isFinished);
                    break;
                case NOT_FINISHED:
                    dataProvider.addFilter(media -> !media.isFinished());
                    break;
                case ALL:
                default:
                    // No filter is applied for "All"
                    break;
            }
        }

    }

    private Set<String> getAllPlatforms(WatchList watchList) {
        // Extract all unique platforms from the media list
        // return mediaList.stream().map(Media::getPlatform).collect(Collectors.toSet());
        return watchList.getItems().stream().map(Media::getPlatform).collect(Collectors.toSet());
    }

    private void openViewDialog(Progress progress) {
        Dialog viewDialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Media details
        formLayout.addFormItem(new Span(progress.getMedia().getTitle()), TITLE);
        formLayout.addFormItem(new Span(progress.getMedia().getPlatform()), PLATFORM);

        // Progress details
        formLayout.addFormItem(new Span(progress.isFavorite() ? "Yes" : "No"), FAVORITE);
        formLayout.addFormItem(new Span(progress.isFinished() ? "Yes" : "No"), FINISHED);

        // for each entry, create a ListItem with the key and value
        UnorderedList progressList = new UnorderedList();
        for (Map.Entry<String, Integer> entry : progress.getProgress().entrySet()) {
            progressList.add(new ListItem(entry.getKey() + ": " + entry.getValue()));
        }

        // add a section for the progress details
        Details details = new Details("Progress Details", progressList);
        details.setOpened(true);

        // Close button
        Button closeButton = new Button("Close", event -> viewDialog.close());

        viewDialog.add(formLayout, details, closeButton);
        viewDialog.open();
    }

    private void openEditDialog(Progress progress) {
        Dialog editDialog = new Dialog();
        FormLayout formLayout = new FormLayout();
        Map<String, Integer> progressMap = new HashMap<>(progress.getProgress());  // Editable progress map

        // add checkboxes for favorite and finished
        // Checkbox for isFavorite
        Checkbox favoriteCheckbox = new Checkbox(FAVORITE);
        favoriteCheckbox.setValue(progress.isFavorite());

        // Checkbox for isFinished (assuming the Progress interface has an isFinished method)
        Checkbox finishedCheckbox = new Checkbox(FINISHED);
        finishedCheckbox.setValue(progress.isFinished());

        formLayout.addFormItem(favoriteCheckbox, "Is Favorite");
        formLayout.addFormItem(finishedCheckbox, "Is Finished");

        // Handle Series progress
        if (progress.getMedia() instanceof Series series) {

            // For each season, create an input field along with a label showing total episodes
            for (Map.Entry<String, Integer> entry : series.seasons().entrySet()) {
                String season = entry.getKey();
                Integer totalEpisodes = entry.getValue();

                TextField seasonField = new TextField("Progress for " + season);
                seasonField.setValue(progressMap.getOrDefault(season, 0).toString());

                // Create a label showing the total number of episodes (greys out)
                Span totalEpisodesLabel = new Span("Total episodes: " + totalEpisodes);
                totalEpisodesLabel.getStyle().set("color", "grey");

                // Add the TextField and the label to the form layout
                formLayout.addFormItem(new HorizontalLayout(seasonField, totalEpisodesLabel), season);

                // Update progress map when the field changes
                seasonField.addValueChangeListener(e -> progressMap.put(season, Integer.parseInt(e.getValue())));
            }
        } else if (progress instanceof VideoGameProgress) {
            // Retrieve the progress map (deep copy to work with)

            // TODO: replace with Static labels

            // Create input fields for each progress type
            TextField mainProgressField = new TextField("Main Progress (%)");
            mainProgressField.setValue(progressMap.getOrDefault("main", 0).toString());
            formLayout.addFormItem(mainProgressField, "Main");
            mainProgressField.addValueChangeListener(e -> progressMap.put("main", Integer.parseInt(e.getValue())));

            TextField sideProgressField = new TextField("Side Progress (%)");
            sideProgressField.setValue(progressMap.getOrDefault("side", 0).toString());
            formLayout.addFormItem(sideProgressField, "Side");
            sideProgressField.addValueChangeListener(e -> progressMap.put("side", Integer.parseInt(e.getValue())));

            TextField collectiblesProgressField = new TextField("Collectibles Progress (%)");
            collectiblesProgressField.setValue(progressMap.getOrDefault("collectibles", 0).toString());
            formLayout.addFormItem(collectiblesProgressField, "Collectibles");
            collectiblesProgressField.addValueChangeListener(e -> progressMap.put("collectibles", Integer.parseInt(e.getValue())));

            TextField achievementsProgressField = new TextField("Achievements Progress (%)");
            achievementsProgressField.setValue(progressMap.getOrDefault("achievements", 0).toString());
            formLayout.addFormItem(achievementsProgressField, "Achievements");

            // Save button
//            Button saveButton = new Button("Save", event -> {
//                try {
//                    // Update the progress map with the new values
//                    progressMap.put("main", Integer.parseInt(mainProgressField.getValue()));
//                    progressMap.put("side", Integer.parseInt(sideProgressField.getValue()));
//                    progressMap.put("collectibles", Integer.parseInt(collectiblesProgressField.getValue()));
//                    progressMap.put("achievements", Integer.parseInt(achievementsProgressField.getValue()));
//
//                    // Update the progress in the original VideoGameProgress object
//                    videoGameProgress.getProgress().putAll(progressMap);
//
//                    Notification.show("Video game progress updated!");
//
//                    editDialog.close();
//                } catch (NumberFormatException e) {
//                    Notification.show("Invalid input. Please enter valid percentages.", 3000, Notification.Position.MIDDLE);
//                }
//            });
//
//            Button cancelButton = new Button("Cancel", event -> editDialog.close());
//
//            editDialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));
//            editDialog.open();
        }
        // TODO: enable when Movie is implemented
//        else if (progress.getMedia() instanceof Movie movie) {
//            // For Movie, allow editing the percentage progress
//            TextField movieProgressField = new TextField("Progress (%)");
//            movieProgressField.setValue(progress.getProgress().get("Progress").toString());
//            formLayout.add(movieProgressField);
//
//            // Update progress map when the field changes
//            movieProgressField.addValueChangeListener(e -> {
//                progress.getProgress().put("Progress", Integer.parseInt(e.getValue()));
//            });
//        }

        // Save button
        Button saveButton = new Button("Save", event -> {
            logger.info("Updated progressMap: " + progressMap.toString());

            // ceate a new Progress object with the updated progress map
            Progress updatedProgress  = null;
            if (progress.getMedia() instanceof Series) {
                updatedProgress = new SeriesProgress(progress.getId(), finishedCheckbox.getValue(), (Series) progress.getMedia(), progressMap, favoriteCheckbox.getValue());
            } else if (progress instanceof VideoGameProgress) {
                updatedProgress = new VideoGameProgress(progress.getId(), progressMap, finishedCheckbox.getValue(), (VideoGame) progress.getMedia(), favoriteCheckbox.getValue());
            }

            if (updatedProgress == null) {
                Notification.show("Failed to update progress");
                return;
            }
            logger.info("Updated progress: {}", updatedProgress);
            var user = userService.getLoggedInUser();
            userService.updateProgress(user, updatedProgress);

            Notification.show("Progress updated!");

            // update the grid
            populateProgressGrid(progressGrid);
            editDialog.close();
        });

        Button cancelButton = new Button("Cancel", event -> editDialog.close());

        editDialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));
        editDialog.open();
    }



    private void populateProgressGrid(Grid<Progress> progressGrid) {
        Set<Progress> progresses = watchlistService.getProgressForWatchlist(currentWatchList);
        dataProvider = new ListDataProvider<>(progresses);
        progressGrid.setDataProvider(dataProvider);
//        progressGrid.setItems(progresses);
    }


    private void openMovieDialog() {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Fields for Movie creation
        TextField titleField = new TextField("Title");
        TextField platformField = new TextField("Platform");
        TextField directorField = new TextField("Director");
        TextField durationField = new TextField("Duration (in minutes)");
        TextField genreField = new TextField("Genres (comma-separated)");
        TextField urlField = new TextField("URL (optional)");

        // Fields for tags
        TextField tagsKeyField = new TextField("Tag Key");
        TextField tagsValueField = new TextField("Tag Value");
        Div tagContainer = new Div();  // Container for displaying added tags
        Map<String, String> tags = new HashMap<>();

        Button addTagButton = new Button("Add Tag", event -> {
            String key = tagsKeyField.getValue();
            String value = tagsValueField.getValue();

            if (!key.isEmpty() && !value.isEmpty()) {
                tags.put(key, value);
                Notification.show("Tag added: " + key + " = " + value);
                tagContainer.add(new Span(key + ": " + value));  // Display the added tag
                tagsKeyField.clear();
                tagsValueField.clear();
            } else {
                Notification.show("Tag key and value must not be empty.", 3000, Notification.Position.MIDDLE);
            }
        });

        // Save button to handle the creation of the Movie
        Button saveButton = new Button("Save", event -> {
            try {
                // Parse input values
                String title = titleField.getValue();
                String platform = platformField.getValue();
                String director = directorField.getValue();
                int duration = Integer.parseInt(durationField.getValue());
                Set<String> genres = new HashSet<>(Arrays.asList(genreField.getValue().split(",\\s*")));
                Optional<String> url = urlField.isEmpty() ? Optional.empty() : Optional.of(urlField.getValue());
                Optional<Map<String, String>> optionalTags = tags.isEmpty() ? Optional.empty() : Optional.of(tags);

                // Create new Movie and add it to the WatchList
                Movie newMovie = new Movie(0, title, platform, director, duration, genres, url, optionalTags);
                currentWatchList.getItems().add(newMovie);

                var user = userService.getLoggedInUser();
                // long id, Movie movie, Map<String, Integer> progress, boolean favorite, boolean finished
                MovieProgress movieProgress = new MovieProgress(0, newMovie, Map.of(), false, false);
                movieService.save(newMovie);
                userService.addProgress(user, movieProgress);
                populateProgressGrid(progressGrid);

                Notification.show("New movie added: " + title);

                // Clear the fields
                titleField.clear();
                platformField.clear();
                directorField.clear();
                durationField.clear();
                genreField.clear();
                urlField.clear();
                tags.clear();
                tagContainer.removeAll();

                // Close the dialog
                dialog.close();
            } catch (NumberFormatException e) {
                Notification.show("Invalid input. Please enter valid numbers for ID and duration.", 3000, Notification.Position.TOP_CENTER);
            }
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        // Add components to the dialog
        formLayout.add(titleField, platformField, directorField, durationField, genreField, urlField);
        dialog.add(formLayout, tagsKeyField, tagsValueField, addTagButton, tagContainer, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }


    // Method to open the Series creation dialog
    private void openSeriesDialog() {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Fields for Series creation
        TextField titleField = new TextField(TITLE);
        TextField platformField = new TextField(PLATFORM);
        TextField genreField = new TextField("Genres (comma-separated)");  // Comma-separated genres
        TextField urlField = new TextField("URL (optional)");  // Optional URL
        TextField releaseYearField = new TextField("Release Year");  // Release year
        TextField endYearField = new TextField("End Year (optional)");  // Optional end year

        // First season default values
        TextField seasonOneField = new TextField("Season 1");
        seasonOneField.setValue("Season 1");  // Pre-fill the season name
        TextField episodesField = new TextField("Episodes (Season 1)");
        episodesField.setValue("10");  // Default 10 episodes for Season 1

        // Container for seasons
        Div seasonContainer = new Div();
        seasonContainer.add(seasonOneField, episodesField);

        // To track the number of seasons added
        Map<String, Integer> seasons = new HashMap<>();
        seasons.put("Season 1", 10);  // Default season

        // Add a button to add more seasons
        Button addSeasonButton = new Button("Add Season", event -> {
            int nextSeasonNumber = seasons.size() + 1;  // Calculate next season number
            String nextSeasonName = "Season " + nextSeasonNumber;

            // Create text fields for the new season
            TextField nextSeasonField = new TextField(nextSeasonName);
            nextSeasonField.setValue(nextSeasonName);  // Name of the season
            TextField nextEpisodesField = new TextField("Episodes (" + nextSeasonName + ")");
            nextEpisodesField.setValue(episodesField.getValue());  // Copy episodes from the first season

            // Add the new season to the container and the map
            seasonContainer.add(nextSeasonField, nextEpisodesField);
            seasons.put(nextSeasonName, Integer.parseInt(nextEpisodesField.getValue()));
        });

        // Tags (Key/Value pairs)
        Map<String, String> tags = new HashMap<>();
        Div tagContainer = new Div();  // Container for dynamically added tag fields
        Button addTagButton = new Button("Add Tag", event -> {
            TextField keyField = new TextField("Key");
            TextField valueField = new TextField("Value");
            tagContainer.add(new HorizontalLayout(keyField, valueField));

            // Add the key-value pair to the map when both fields are filled
            Button saveTagButton = new Button("Save Tag", saveEvent -> {
                if (!keyField.isEmpty() && !valueField.isEmpty()) {
                    tags.put(keyField.getValue(), valueField.getValue());
                    Notification.show("Tag added: " + keyField.getValue() + " = " + valueField.getValue());
                    keyField.setReadOnly(true);
                    valueField.setReadOnly(true);
                }
            });
            tagContainer.add(saveTagButton);
        });

        // Save button to handle the creation of the Series
        Button saveButton = new Button("Save", event -> {
            try {
                // Parse input values
                String title = titleField.getValue();
                String platform = platformField.getValue();
                String[] genresArray = genreField.getValue().split(",\\s*");  // Split by comma and remove extra spaces
                Set<String> genres = new HashSet<>(Arrays.asList(genresArray));

                Optional<String> url = urlField.isEmpty() ? Optional.empty() : Optional.of(urlField.getValue());

                int releaseYear = Integer.parseInt(releaseYearField.getValue());
                LocalDate releaseDate = LocalDate.of(releaseYear, 1, 2);  // Set to 02-01-<Year>

                Optional<LocalDate> endYear = endYearField.isEmpty() ? Optional.empty() :
                        Optional.of(LocalDate.of(Integer.parseInt(endYearField.getValue()), 1, 2));

                // Parse season data (update the first season if the user has changed it)
                seasons.put("Season 1", Integer.parseInt(episodesField.getValue()));

                // Create new Series and add it to the WatchList
                // Please read the Series record, and generate a proper constructor call
                Series newSeries = new Series(0, title, genres, seasons, platform, url, Optional.of(releaseDate), endYear, Optional.of(tags));
                currentWatchList.getItems().add(newSeries);

                // Create SeriesProgress for this new series
                // create ProgressMap for each Season, setting the progress to 1
                Map<String, Integer> progressMap = new HashMap<>();
                seasons.forEach((season, episodes) -> progressMap.put(season, 0));
                var user = userService.getLoggedInUser();
                SeriesProgress seriesProgress = new SeriesProgress(0, false, newSeries, progressMap, false);
                seriesService.addSeries(newSeries);
                userService.addProgress(user, seriesProgress);
                populateProgressGrid(progressGrid);
                Notification.show("New series added and progress created for: " + title);

                // Clear the fields
                titleField.clear();
                platformField.clear();
                genreField.clear();
                urlField.clear();
                releaseYearField.clear();
                endYearField.clear();
                seasonContainer.removeAll();
                seasonContainer.add(seasonOneField, episodesField);  // Reset the first season
                tagContainer.removeAll();  // Clear tags

                // Close the dialog
                dialog.close();
            } catch (NumberFormatException e) {
                Notification.show("Invalid input for release year or number of episodes.");
            }
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        // Add components to the dialog
        formLayout.add(titleField, platformField, genreField, urlField, releaseYearField, endYearField);
        dialog.add(formLayout, seasonContainer, addSeasonButton, tagContainer, addTagButton, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }

    private void openVideoGameDialog() {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Fields for VideoGame creation
        TextField titleField = new TextField(TITLE);
        TextField platformField = new TextField(PLATFORM);
        TextField genreField = new TextField("Genres (comma-separated)");
        TextField publisherField = new TextField("Publisher");
        TextField developerField = new TextField("Developer");
        TextField yearField = new TextField("Release Year");
        TextField tagsKeyField = new TextField("Tag Key");
        TextField tagsValueField = new TextField("Tag Value");

        // Container for tags
        Div tagContainer = new Div();
        Map<String, String> tags = new HashMap<>();

        Button addTagButton = new Button("Add Tag", event -> {
            String key = tagsKeyField.getValue();
            String value = tagsValueField.getValue();

            if (!key.isEmpty() && !value.isEmpty()) {
                tags.put(key, value);
                Notification.show("Tag added: " + key + " = " + value);
                tagContainer.add(new Span(key + ": " + value));  // Display the added tag
                tagsKeyField.clear();
                tagsValueField.clear();
            } else {
                Notification.show("Tag key and value must not be empty.", 3000, Notification.Position.MIDDLE);
            }
        });

        // Save button to handle the creation of the VideoGame
        Button saveButton = new Button("Save", event -> {
            try {
                // Parse input values
                String title = titleField.getValue();
                String platform = platformField.getValue();
                String[] genresArray = genreField.getValue().split(",\\s*");
                Set<String> genres = new HashSet<>(Arrays.asList(genresArray));
                String publisher = publisherField.getValue();
                String developer = developerField.getValue();
                int year = Integer.parseInt(yearField.getValue());
                Optional<Map<String, String>> optionalTags = tags.isEmpty() ? Optional.empty() : Optional.of(tags);

                // Create new VideoGame and add it to the WatchList
                VideoGame newVideoGame = new VideoGame(0, title, platform, genres, publisher, developer, year, optionalTags);
                currentWatchList.getItems().add(newVideoGame);

                // Create SeriesProgress for this new series
                var user = userService.getLoggedInUser();
                VideoGameProgress videoGameProgress = new VideoGameProgress(0, Map.of(),false, newVideoGame,  false);
                videoGameService.addVideoGame(newVideoGame);
                userService.addProgress(user, videoGameProgress);
                populateProgressGrid(progressGrid);

                Notification.show("New video game added: " + title);

                // Clear the fields
                titleField.clear();
                platformField.clear();
                genreField.clear();
                publisherField.clear();
                developerField.clear();
                yearField.clear();
                tags.clear();
                tagContainer.removeAll();

                // Close the dialog
                dialog.close();
            } catch (NumberFormatException e) {
                Notification.show("Invalid input for ID or Year.", 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        // Add components to the dialog
        formLayout.add(titleField, platformField, genreField, publisherField, developerField, yearField);
        dialog.add(formLayout, tagsKeyField, tagsValueField, addTagButton, tagContainer, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }


}
