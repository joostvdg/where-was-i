package net.joostvdg.wwi.media;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.annotation.security.PermitAll;
import net.joostvdg.wwi.main.Labels;
import net.joostvdg.wwi.main.MainView;
import net.joostvdg.wwi.main.ViewNotifications;
import net.joostvdg.wwi.tracking.WatchList;
import net.joostvdg.wwi.tracking.WatchlistService;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;

@Route(value = "video-games", layout = MainView.class)
@PageTitle("Movies | Where Was I?")
@PermitAll
public class VideoGameListView extends VerticalLayout {

    private Grid<VideoGame> videoGameGrid;
    private ListDataProvider<VideoGame> dataProvider;
    private TextField titleFilter;
    private TextField platformFilter;
    private ComboBox<Integer> yearFilter;
    private ComboBox<String> genreFilter;

    private final WatchlistService watchlistService;
    private final VideoGameService videoGameService;
    private final UserService userService;

    public VideoGameListView(VideoGameService videoGameService, WatchlistService watchlistService, UserService userService) {
        this.watchlistService = watchlistService;
        this.videoGameService = videoGameService;
        this.userService = userService;
        // Fetch all video games from the service (or repository)
        List<VideoGame> videoGameList = videoGameService.findAll();
        dataProvider = new ListDataProvider<>(videoGameList);

        // Initialize Grid
        videoGameGrid = new Grid<>(VideoGame.class);
        videoGameGrid.setItems(dataProvider);
        configureGridColumns();

        // Create search fields
        titleFilter = new TextField(Labels.TITLE);
        platformFilter = new TextField(Labels.PLATFORM);
        yearFilter = new ComboBox<>(Labels.YEAR);
        yearFilter.setItems(getYears(videoGameList));
        yearFilter.setPlaceholder("Select year");

        genreFilter = new ComboBox<>("Genre");
        genreFilter.setItems(getAllGenres(videoGameList));
        genreFilter.setPlaceholder("Select genre");

        // Add change listeners for filtering
        titleFilter.addValueChangeListener(e -> applyFilters());
        platformFilter.addValueChangeListener(e -> applyFilters());
        yearFilter.addValueChangeListener(e -> applyFilters());
        genreFilter.addValueChangeListener(e -> applyFilters());

        // Layout for search filters
        HorizontalLayout filterLayout = new HorizontalLayout(titleFilter, platformFilter, yearFilter, genreFilter);
        add(filterLayout, videoGameGrid);

        // Create "New Video Game" button
        Button newVideoGameButton = new Button("New Video Game", e -> openVideoGameCreationDialog());
        add(newVideoGameButton);
    }

    private void configureGridColumns() {
        videoGameGrid.removeAllColumns();

        // Add "Action" column with buttons for View, Edit, and Add to Watchlist
        videoGameGrid.addColumn(new ComponentRenderer<>(videoGame -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();

            // View Button
            Icon viewIcon = new Icon(VaadinIcon.EYE);
            viewIcon.getStyle().set("cursor", "pointer");
            viewIcon.setColor("white");
            viewIcon.addClickListener(e -> openVideoGameDetailsDialog(videoGame));

            // Edit Button
            Icon editIcon = new Icon(VaadinIcon.EDIT);
            editIcon.getStyle().set("cursor", "pointer");
            editIcon.setColor("red");
            editIcon.addClickListener(e -> openVideoGameEditDialog(videoGame));

            // Add to Watchlist Button
            Icon addToWatchlistIcon = new Icon(VaadinIcon.LIST);
            addToWatchlistIcon.getStyle().set("cursor", "pointer");
            addToWatchlistIcon.setColor("green");
            addToWatchlistIcon.addClickListener(e -> openAddToWatchlistDialog(videoGame));

            actionsLayout.add(viewIcon, editIcon, addToWatchlistIcon);
            return actionsLayout;
        })).setHeader("Actions");
        videoGameGrid.addColumn(VideoGame::title).setHeader(Labels.TITLE);
        videoGameGrid.addColumn(VideoGame::platform).setHeader(Labels.PLATFORM);
        videoGameGrid.addColumn(VideoGame::publisher).setHeader(Labels.PUBLISHER);
        videoGameGrid.addColumn(VideoGame::developer).setHeader(Labels.DEVELOPER);
        videoGameGrid.addColumn(VideoGame::year).setHeader(Labels.RELEASE_YEAR);
        videoGameGrid.addColumn(videoGame -> String.join(", ", videoGame.genre())).setHeader(Labels.GENRES);
        videoGameGrid.addColumn(videoGame -> videoGame.tags().map(tags -> tags.toString()).orElse("No tags")).setHeader("Tags");
    }

    private void applyFilters() {
        dataProvider.clearFilters();

        // Apply title filter
        if (!titleFilter.isEmpty()) {
            dataProvider.addFilter(videoGame -> videoGame.title().toLowerCase().contains(titleFilter.getValue().toLowerCase()));
        }

        // Apply platform filter
        if (!platformFilter.isEmpty()) {
            dataProvider.addFilter(videoGame -> videoGame.platform().toLowerCase().contains(platformFilter.getValue().toLowerCase()));
        }

        // Apply year filter
        if (yearFilter.getValue() != null) {
            dataProvider.addFilter(videoGame -> videoGame.year() == yearFilter.getValue());
        }

        // Apply genre filter
        if (genreFilter.getValue() != null) {
            dataProvider.addFilter(videoGame -> videoGame.genre().contains(genreFilter.getValue()));
        }
    }

    private Set<Integer> getYears(List<VideoGame> videoGameList) {
        return videoGameList.stream().map(VideoGame::year).collect(Collectors.toSet());
    }

    private Set<String> getAllGenres(List<VideoGame> videoGameList) {
        return videoGameList.stream().flatMap(videoGame -> videoGame.genre().stream()).collect(Collectors.toSet());
    }

    private void openVideoGameDetailsDialog(VideoGame videoGame) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Display video game details
        formLayout.addFormItem(new TextField(Labels.TITLE, videoGame.title()), Labels.TITLE);
        formLayout.addFormItem(new TextField(Labels.PLATFORM, videoGame.platform()), Labels.PLATFORM);
        formLayout.addFormItem(new TextField(Labels.PUBLISHER, videoGame.publisher()), Labels.PUBLISHER);
        formLayout.addFormItem(new TextField(Labels.DEVELOPER, videoGame.developer()), Labels.DEVELOPER);
        formLayout.addFormItem(new TextField(Labels.YEAR, String.valueOf(videoGame.year())), Labels.YEAR);
        formLayout.addFormItem(new TextField(Labels.GENRES, String.join(", ", videoGame.genre())), Labels.GENRES);
        formLayout.addFormItem(new TextField(Labels.TAGS, videoGame.tags().map(Object::toString).orElse("No tags")), "Tags");

        // All fields should be read-only
        formLayout.getChildren().filter( child -> child instanceof TextField)
                .map(child -> (TextField) child)
                .forEach(field -> field.setReadOnly(true));

        Button closeButton = new Button("Close", e -> dialog.close());
        dialog.add(formLayout, closeButton);
        dialog.open();
    }

    private void openVideoGameEditDialog(VideoGame videoGame) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Fields for Video Game editing
        TextField titleField = new TextField("Title", videoGame.title());
        TextField platformField = new TextField("Platform", videoGame.platform());
        TextField publisherField = new TextField("Publisher", videoGame.publisher());
        TextField developerField = new TextField("Developer", videoGame.developer());
        TextField yearField = new TextField("Year", String.valueOf(videoGame.year()));
        TextField genreField = new TextField("Genres (comma-separated)", String.join(", ", videoGame.genre()));

        // Save button to handle the Video Game edit
        Button saveButton = new Button("Save", event -> {
            try {
                videoGameService.updateVideoGame(videoGame);
                videoGameGrid.getDataProvider().refreshItem(videoGame);
                Notification.show("Video game updated: " + titleField.getValue());
                dialog.close();
            } catch (NumberFormatException e) {
                Notification.show("Invalid input for year.", 3000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        // Add components to the form layout
        formLayout.add(titleField, platformField, publisherField, developerField, yearField, genreField);
        dialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }

    private void openAddToWatchlistDialog(VideoGame videoGame) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Fetch available watchlists (you may have a service to get this)
        User user = userService.getLoggedInUser();
        List<WatchList> availableWatchlists = watchlistService.getWatchListsForUser(user);

        // Dropdown to select a watchlist
        ComboBox<WatchList> watchlistComboBox = new ComboBox<>("Select Watchlist");
        watchlistComboBox.setItems(availableWatchlists);
        watchlistComboBox.setItemLabelGenerator(WatchList::getName);

        // Add button to add video game to selected watchlist
        Button addToWatchlistButton = new Button("Add", event -> {
            WatchList selectedWatchlist = watchlistComboBox.getValue();

            if (selectedWatchlist == null) {
                ViewNotifications.showErrorNotification("Please select a watchlist");
                return;
            }

            if (selectedWatchlist.getItems().contains(videoGame)) {
                ViewNotifications.showErrorNotification("Video game already in watchlist: " + selectedWatchlist.getName());
                return;
            }

            selectedWatchlist.getItems().add(videoGame);
            ViewNotifications.showSuccessNotification("Video game added to watchlist: " + selectedWatchlist.getName());
            dialog.close();

        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        formLayout.add(watchlistComboBox);
        dialog.add(formLayout, new HorizontalLayout(addToWatchlistButton, cancelButton));
        dialog.open();
    }

    // TODO: should be moved to a separate class
    private void openVideoGameCreationDialog() {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        // Fields for VideoGame creation
        TextField titleField = new TextField(Labels.TITLE);
        TextField platformField = new TextField(Labels.PLATFORM);
        TextField genreField = new TextField(Labels.GENRES_INPUT);
        TextField publisherField = new TextField(Labels.PUBLISHER);
        TextField developerField = new TextField(Labels.DEVELOPER);
        TextField yearField = new TextField(Labels.YEAR);
        TextField tagsKeyField = new TextField(Labels.TAG_KEY);
        TextField tagsValueField = new TextField(Labels.TAG_VALUE);

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
                ViewNotifications.showErrorNotification("Tag key and value must not be empty.");
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
                newVideoGame = videoGameService.addVideoGame(newVideoGame);

                updateGrid(newVideoGame);
                ViewNotifications.showSuccessNotification("New video game added: " + title);

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
                ViewNotifications.showErrorNotification("Invalid input for ID or Year.");
            }
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        // Add components to the form
        formLayout.add(titleField, platformField, genreField, publisherField, developerField, yearField);
        dialog.add(formLayout, tagsKeyField, tagsValueField, addTagButton, tagContainer, new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }

    private void updateGrid(VideoGame videoGame) {
        var videoGameList = dataProvider.getItems();
        videoGameList.add(videoGame);
        dataProvider.refreshAll();
        yearFilter.setItems(getYears((List<VideoGame>) videoGameList));
        genreFilter.setItems(getAllGenres((List<VideoGame>) videoGameList));
    }
}
