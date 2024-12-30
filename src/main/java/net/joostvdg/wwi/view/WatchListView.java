package net.joostvdg.wwi.view;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.data.provider.ListDataProvider;
import jakarta.annotation.security.PermitAll;
import net.joostvdg.wwi.progress.ProgressService;
import net.joostvdg.wwi.watchlist.SharedWatchListItem;
import net.joostvdg.wwi.watchlist.WatchList;
import net.joostvdg.wwi.watchlist.WatchlistService;
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
import net.joostvdg.wwi.media.Progress;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Route(value = "watchlist", layout = MainView.class)
@PermitAll
@PageTitle("Watchlist | Where Was I?")
public class WatchListView extends VerticalLayout {

    private final Logger logger = LoggerFactory.getLogger(WatchListView.class);

    private final transient WatchlistService watchlistService;
    private final transient ProgressService progressService;

    private final Grid<WatchList> grid = new Grid<>(WatchList.class, false);
    private Grid<WatchList> sharedWatchlistGrid;
    private ListDataProvider<WatchList> sharedWatchlistsProvider;

    private final TextField nameField = new TextField("Name");
    private final TextArea descriptionField = new TextArea("Description");
    private final Button saveButton = new Button("Save Watchlist");

    private final UserService userService;

    public WatchListView(WatchlistService watchlistService, ProgressService progressService, UserService userService) {
        this.watchlistService = watchlistService;
        this.progressService = progressService;
        this.userService = userService;


        List<WatchList> sharedWatchlists = watchlistService.findSharedWith(userService.getLoggedInUser());
        sharedWatchlistsProvider = new ListDataProvider<>(sharedWatchlists);
        sharedWatchlistGrid = new Grid<>(WatchList.class, false);
        sharedWatchlistGrid.setItems(sharedWatchlistsProvider);
        configureSharedWatchlistGrid();

        configureGrid();
        configureForm();

        grid.addComponentColumn(watchlist -> {
            Button shareButton = new Button("Share", e -> openShareDialog(watchlist, userService, watchlistService));
            return new HorizontalLayout(shareButton);
        }).setHeader("Actions");

        add(new HorizontalLayout(nameField, descriptionField), saveButton, new VerticalLayout(new H1("My Watch Lists"), grid));
        add(new VerticalLayout(new H1("Shared With Me Watch Lists"), sharedWatchlistGrid));

        saveButton.addClickListener(e -> saveWatchList());
    }

    private void configureSharedWatchlistGrid() {
        sharedWatchlistGrid.addColumn(WatchList::getName).setHeader("Name").setSortable(true);
        sharedWatchlistGrid.addColumn(WatchList::getCreated).setHeader("Created").setSortable(true);
        sharedWatchlistGrid.addColumn(watchList -> watchList.getItems().size()).setHeader("Media Items").setSortable(true);
        sharedWatchlistGrid.addColumn( watchList -> watchList.getOwner().username() +
                " (" + watchList.getOwner().accountNumber() + ", " +
                watchList.getOwner().accountType() + ")"
                ).setHeader("Owner").setSortable(true);
        sharedWatchlistGrid.addItemClickListener(event -> openSharedWatchListDetails(event.getItem()));

    }


    private void openSharedWatchListDetails(WatchList watchList) {
        Dialog detailsDialog = new Dialog();

        VerticalLayout dialogLayout = new VerticalLayout();
        detailsDialog.setDraggable(true);
        detailsDialog.setResizable(true);
        dialogLayout.add(new H1("Shared Watch List Details"));

        List<SharedWatchListItem> items = new ArrayList<>();
        var progressSet = getProgressForWatchlist(watchList);

        logger.info("Found {} progresses and {} items in watchlist", progressSet.size(), watchList.getItems().size());

        for (Media item : watchList.getItems()) {
            for (Progress progress : progressSet) {
                logger.info("Checking progress for media: {} and progress: {}" + item.getId(), progress.getMedia().getId());

                if (progress.getMedia().getId() == item.getId()) {
                    items.add(new SharedWatchListItem(
                            item.getClass().getSimpleName(),
                            item.getTitle(),
                            item.getPlatform(),
                            progress.getSummary()
                    ));
                }
            }
        }

        Grid<SharedWatchListItem> itemGrid = new Grid<>(SharedWatchListItem.class, false);
        itemGrid.addColumn(SharedWatchListItem::title).setHeader("Title").setSortable(true).setAutoWidth(true);
        itemGrid.addColumn(SharedWatchListItem::mediaType).setHeader("Type").setSortable(true).setAutoWidth(true);
        itemGrid.addColumn(SharedWatchListItem::platform).setHeader("Platform").setSortable(true).setAutoWidth(true);
        itemGrid.addColumn(SharedWatchListItem::progressSummary).setHeader("Progress").setSortable(true).setAutoWidth(true);
        itemGrid.setItems(items);
        dialogLayout.add(itemGrid);

        Button closeButton = new Button("Close", e -> detailsDialog.close());
        dialogLayout.add(closeButton);

        detailsDialog.add(dialogLayout);
        detailsDialog.open();
    }

    private Set<Progress> getProgressForWatchlist(WatchList watchList) {
        return progressService.getProgressForUserAndMedia(watchList.getOwner().id(), watchList.getItems());
    }

    private void openShareDialog(WatchList watchlist, UserService userService, WatchlistService watchlistService) {
        Dialog shareDialog = new Dialog();

        // Search field for users
        ComboBox<User> userSearch = new ComboBox<>("Share With");
        userSearch.setItems(userService.getAllUsers());
        userSearch.setItemLabelGenerator(user -> user.username() + " (" + user.accountNumber() + ", " + user.accountType() + ")");

        Button shareButton = new Button("Share", e -> {
            User selectedUser = userSearch.getValue();
            if (selectedUser != null) {
                watchlistService.shareWatchlist(watchlist, selectedUser);
                Notification.show("Watchlist shared with " + selectedUser.username());
                shareDialog.close();
            } else {
                Notification.show("Please select a user to share with.");
            }
        });

        Button cancelButton = new Button("Cancel", e -> shareDialog.close());

        shareDialog.add(new VerticalLayout(userSearch, new HorizontalLayout(shareButton, cancelButton)));
        shareDialog.open();
    }


    private void configureGrid() {
        grid.addColumn(WatchList::getName).setHeader("Name").setSortable(true);
        grid.addColumn(WatchList::getCreated).setHeader("Created").setSortable(true);
        grid.addColumn(watchList -> watchList.getItems().size()).setHeader("Media Items").setSortable(true);
        grid.addColumn(watchList -> watchList.isFavorite() ? "Yes" : "No").setHeader("Favorite").setSortable(true);
        grid.setItems(watchlistService.getWatchListsForUser(userService.getLoggedInUser()));
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

        var watchLists = watchlistService.getWatchListsForUser(userService.getLoggedInUser());
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
