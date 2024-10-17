package net.joostvdg.wwi.tracking;

import jakarta.annotation.security.PermitAll;
import net.joostvdg.wwi.main.MainView;
import net.joostvdg.wwi.media.Media;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Route(value = "watchlist", layout = MainView.class)
@PermitAll
@PageTitle("Watchlist | Where Was I?")
public class WatchListView extends VerticalLayout {

    private final Logger logger = LoggerFactory.getLogger(WatchListView.class);

    private final WatchlistService watchlistService;

    private final Grid<WatchList> grid = new Grid<>(WatchList.class, false);

    private final TextField nameField = new TextField("Name");
    private final TextArea descriptionField = new TextArea("Description");
    private final Button saveButton = new Button("Save Watchlist");

    private final UserService userService;

    private int idCounter = 0;

    public WatchListView(WatchlistService watchlistService, UserService userService) {
        this.watchlistService = watchlistService;
        this.userService = userService;
        configureGrid();
        configureForm();

        add(new HorizontalLayout(nameField, descriptionField), saveButton, grid);

        saveButton.addClickListener(e -> saveWatchList());
    }

    private void configureGrid() {
        grid.addColumn(WatchList::getName).setHeader("Name").setSortable(true);
        grid.addColumn(WatchList::getCreated).setHeader("Created").setSortable(true);
        grid.addColumn(watchList -> watchList.getItems().size()).setHeader("Media Items").setSortable(true);
        grid.addColumn(watchList -> watchList.isFavorite() ? "Yes" : "No").setHeader("Favorite").setSortable(true);
        grid.setItems(watchlistService.getWatchLists());
        grid.addItemClickListener(event -> openWatchListDetails(event.getItem()));
    }

    private void configureForm() {
        nameField.setPlaceholder("Enter watchlist name");
        descriptionField.setPlaceholder("Enter description");
    }

    private void saveWatchList() {
        // Example: Create a simple User and Media objects
        var owner = userService.getLoggedInUser();
        Set<Media> items = new HashSet<>(); // Empty for now, but you can add Series, Movie, or VideoGame here
        Set<User> readShared = new HashSet<>();
        Set<User> writeShared = new HashSet<>();

        WatchList watchList = new WatchList(
                0,
                items,
                owner,
                readShared,
                writeShared,
                nameField.getValue(),
                descriptionField.getValue(),
                Instant.now(),
                Instant.now(),
                false
        );

        watchlistService.addWatchList(watchList);
        logger.info("Watchlist created");

        var watchLists = watchlistService.getWatchLists();
        logger.info("Watchlists contains #" + watchLists.size() + " items");
        grid.setItems(watchLists); // Refresh the grid
        // Clear the form
        nameField.clear();
        descriptionField.clear();
        Notification.show("Watchlist created!");
    }

    private void openWatchListDetails(WatchList watchList) {
        logger.info("Opening details for watchlist: " + watchList.getId());
        if (watchList == null) {
            Notification.show("No watchlist selected");
            logger.warn("No watchlist selected");
            return;
        }

        // Navigate to the details view for the selected watchlist
        Notification.show("Navigating to details of " + watchList.getId());
        getUI().ifPresent(ui -> ui.navigate(WatchListDetailsView.class, Long.toString(watchList.getId())));
    }
}
