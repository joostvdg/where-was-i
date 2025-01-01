/* (C)2024 */
package net.joostvdg.wwi.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
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
import jakarta.annotation.security.PermitAll;
import java.io.Serial;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import net.joostvdg.wwi.media.Series;
import net.joostvdg.wwi.media.SeriesService;
import net.joostvdg.wwi.progress.ProgressService;
import net.joostvdg.wwi.shared.Labels;
import net.joostvdg.wwi.shared.ViewNotifications;
import net.joostvdg.wwi.user.User;
import net.joostvdg.wwi.user.UserService;
import net.joostvdg.wwi.watchlist.WatchList;
import net.joostvdg.wwi.watchlist.WatchlistService;

@Route(value = "series", layout = MainView.class)
@PageTitle("Movies | Where Was I?")
@PermitAll // TODO: limit access to Admin users?
public class SeriesListView extends VerticalLayout {

  @Serial private static final long serialVersionUID = 1L;

  private final Grid<Series> seriesGrid;
  private final ListDataProvider<Series> dataProvider;
  private final TextField titleFilter;
  private final TextField platformFilter;
  private final ComboBox<Integer> releaseYearFilter;
  private final ComboBox<String> genreFilter;

  private final transient SeriesService seriesService;
  private final transient UserService userService;
  private final transient WatchlistService watchlistService;
  private final transient ProgressService progressService;

  public SeriesListView(
      SeriesService seriesService,
      UserService userService,
      WatchlistService watchlistService,
      ProgressService progressService) {
    this.seriesService = seriesService;
    this.userService = userService;
    this.watchlistService = watchlistService;
    this.progressService = progressService;
    // Fetch all series from the service (or repository)
    List<Series> seriesList = seriesService.findAll();
    dataProvider = new ListDataProvider<>(seriesList);

    // Initialize Grid
    seriesGrid = new Grid<>(Series.class);
    seriesGrid.setItems(dataProvider);
    configureGridColumns();

    // Create search fields
    titleFilter = new TextField(Labels.TITLE);
    platformFilter = new TextField(Labels.PLATFORM);

    releaseYearFilter = new ComboBox<>("Release Year");
    releaseYearFilter.setItems(getReleaseYears(seriesList)); // Populate with unique release years
    releaseYearFilter.setPlaceholder("Select year");

    genreFilter = new ComboBox<>("Genre");
    genreFilter.setItems(getAllGenres(seriesList)); // Populate with unique genres
    genreFilter.setPlaceholder("Select genre");

    // Add change listeners for filtering
    titleFilter.addValueChangeListener(e -> applyFilters());
    platformFilter.addValueChangeListener(e -> applyFilters());
    releaseYearFilter.addValueChangeListener(e -> applyFilters());
    genreFilter.addValueChangeListener(e -> applyFilters());

    // Layout for search filters
    HorizontalLayout filterLayout =
        new HorizontalLayout(titleFilter, platformFilter, releaseYearFilter, genreFilter);
    add(filterLayout, seriesGrid);

    // Create "New Series" button
    Button newSeriesButton = new Button("New Series", e -> openSeriesCreationDialog());
    add(newSeriesButton);
  }

  private void openSeriesCreationDialog() {
    Dialog dialog = new Dialog();
    FormLayout formLayout = new FormLayout();

    // Fields for Series creation
    TextField titleField = new TextField(Labels.TITLE);
    TextField platformField = new TextField(Labels.PLATFORM);
    TextField genreField = new TextField(Labels.GENRES_INPUT); // Comma-separated genres
    TextField urlField = new TextField(Labels.URL_INPUT); // Optional URL
    TextField releaseYearField = new TextField(Labels.RELEASE_YEAR); // Release year
    TextField endYearField = new TextField(Labels.END_YEAR_INPUT); // Optional end year

    // First season default values
    TextField seasonOneField = new TextField("Season 1");
    seasonOneField.setValue("Season 1"); // Pre-fill the season name
    TextField episodesField = new TextField("Episodes (Season 1)");
    episodesField.setValue("10"); // Default 10 episodes for Season 1

    // Container for seasons
    Div seasonContainer = new Div();
    seasonContainer.add(seasonOneField, episodesField);

    // To track the number of seasons added
    Map<String, Integer> seasons = new HashMap<>();
    seasons.put("Season 1", 10); // Default season

    // Add a button to add more seasons
    Button addSeasonButton =
        new Button(
            "Add Season",
            event -> {
              int nextSeasonNumber = seasons.size() + 1; // Calculate next season number
              String nextSeasonName = "Season " + nextSeasonNumber;

              // Create text fields for the new season
              TextField nextSeasonField = new TextField(nextSeasonName);
              nextSeasonField.setValue(nextSeasonName); // Name of the season
              TextField nextEpisodesField = new TextField("Episodes (" + nextSeasonName + ")");
              nextEpisodesField.setValue(
                  episodesField.getValue()); // Copy episodes from the first season

              // Add the new season to the container and the map
              seasonContainer.add(nextSeasonField, nextEpisodesField);
              seasons.put(nextSeasonName, Integer.parseInt(nextEpisodesField.getValue()));
            });

    // Tags (Key/Value pairs)
    Map<String, String> tags = new HashMap<>();
    Div tagContainer = new Div(); // Container for dynamically added tag fields
    Button addTagButton =
        new Button(
            "Add Tag",
            event -> {
              TextField keyField = new TextField("Key");
              TextField valueField = new TextField("Value");
              tagContainer.add(new HorizontalLayout(keyField, valueField));

              // Add the key-value pair to the map when both fields are filled
              Button saveTagButton =
                  new Button(
                      "Save Tag",
                      saveEvent -> {
                        if (!keyField.isEmpty() && !valueField.isEmpty()) {
                          tags.put(keyField.getValue(), valueField.getValue());
                          Notification.show(
                              "Tag added: " + keyField.getValue() + " = " + valueField.getValue());
                          keyField.setReadOnly(true);
                          valueField.setReadOnly(true);
                        }
                      });
              tagContainer.add(saveTagButton);
            });

    // Save button to handle the creation of the Series
    Button saveButton =
        new Button(
            "Save",
            event -> {
              try {
                // Parse input values
                String title = titleField.getValue();
                String platform = platformField.getValue();
                String[] genresArray =
                    genreField.getValue().split(",\\s*"); // Split by comma and remove extra spaces
                Set<String> genres = new HashSet<>(Arrays.asList(genresArray));

                Optional<String> url =
                    urlField.isEmpty() ? Optional.empty() : Optional.of(urlField.getValue());

                int releaseYear = Integer.parseInt(releaseYearField.getValue());
                LocalDate releaseDate = LocalDate.of(releaseYear, 1, 2); // Set to 02-01-<Year>

                Optional<LocalDate> endYear =
                    endYearField.isEmpty()
                        ? Optional.empty()
                        : Optional.of(
                            LocalDate.of(Integer.parseInt(endYearField.getValue()), 1, 2));

                // Parse season data (update the first season if the user has changed it)
                seasons.put("Season 1", Integer.parseInt(episodesField.getValue()));

                // Create new Series and add it to the WatchList
                // Please read the Series record, and generate a proper constructor call
                Series newSeries =
                    new Series(
                        0,
                        title,
                        genres,
                        seasons,
                        platform,
                        url,
                        Optional.of(releaseDate),
                        endYear,
                        Optional.of(tags));
                newSeries = seriesService.addSeries(newSeries);
                refreshGrid(newSeries);
                ViewNotifications.showSuccessNotification(
                    "New series added and progress created for: " + title);

                // Clear the fields
                titleField.clear();
                platformField.clear();
                genreField.clear();
                urlField.clear();
                releaseYearField.clear();
                endYearField.clear();
                seasonContainer.removeAll();
                seasonContainer.add(seasonOneField, episodesField); // Reset the first season
                tagContainer.removeAll(); // Clear tags

                // Close the dialog
                dialog.close();
              } catch (NumberFormatException e) {
                ViewNotifications.showErrorNotification(
                    "Invalid input for release year or number of episodes.");
              }
            });

    Button cancelButton = new Button("Cancel", event -> dialog.close());

    // Add components to the dialog
    formLayout.add(titleField, platformField, genreField, urlField, releaseYearField, endYearField);
    dialog.add(
        formLayout,
        seasonContainer,
        addSeasonButton,
        tagContainer,
        addTagButton,
        new HorizontalLayout(saveButton, cancelButton));
    dialog.open();
  }

  private void refreshGrid(Series newSeries) {
    var seriesList = dataProvider.getItems();
    seriesList.add(newSeries);
    dataProvider.refreshAll();
    releaseYearFilter.setItems(getReleaseYears((List<Series>) seriesList));
    genreFilter.setItems(getAllGenres((List<Series>) seriesList));
  }

  private void configureGridColumns() {
    seriesGrid.removeAllColumns();

    // Action column (icons for view, edit, add to WatchList)
    seriesGrid
        .addColumn(
            new ComponentRenderer<>(
                series -> {
                  HorizontalLayout actionsLayout = new HorizontalLayout();

                  // View Icon
                  Icon viewIcon = VaadinIcon.EYE.create();
                  viewIcon.getStyle().set("cursor", "pointer");
                  viewIcon.setColor("white");
                  viewIcon.addClickListener(e -> openSeriesDetailsDialog(series));

                  // Edit Icon
                  Icon editIcon = VaadinIcon.EDIT.create();
                  editIcon.getStyle().set("cursor", "pointer");
                  editIcon.setColor("red");
                  editIcon.addClickListener(e -> openSeriesEditDialog(series));

                  // Add to WatchList Icon
                  Icon addToWatchListIcon = VaadinIcon.LIST.create();
                  addToWatchListIcon.getStyle().set("cursor", "pointer");
                  addToWatchListIcon.setColor("green");
                  addToWatchListIcon.addClickListener(e -> openAddToWatchListDialog(series));

                  actionsLayout.add(viewIcon, editIcon, addToWatchListIcon);
                  return actionsLayout;
                }))
        .setHeader("Actions");

    // Add other columns for Series attributes
    seriesGrid.addColumn(Series::title).setHeader(Labels.TITLE);
    seriesGrid.addColumn(Series::platform).setHeader(Labels.PLATFORM);
    seriesGrid.addColumn(series -> String.join(", ", series.genre())).setHeader(Labels.GENRES);
    seriesGrid.addColumn(series -> series.seasons().toString()).setHeader(Labels.SEASONS);
    seriesGrid
        .addColumn(series -> series.releaseYear().map(LocalDate::toString).orElse(Labels.NA))
        .setHeader(Labels.RELEASE_YEAR);
    seriesGrid
        .addColumn(series -> series.endYear().map(LocalDate::toString).orElse("Ongoing"))
        .setHeader(Labels.END_YEAR);
    seriesGrid.addColumn(series -> series.url().orElse("")).setHeader(Labels.URL);
    seriesGrid
        .addColumn(series -> series.tags().map(Map::toString).orElse("No tags"))
        .setHeader(Labels.TAGS);
  }

  private void applyFilters() {
    dataProvider.clearFilters();

    // Apply title filter
    if (!titleFilter.isEmpty()) {
      dataProvider.addFilter(
          series ->
              series
                  .title()
                  .toLowerCase(Locale.ROOT)
                  .contains(titleFilter.getValue().toLowerCase(Locale.ROOT)));
    }

    // Apply platform filter
    if (!platformFilter.isEmpty()) {
      dataProvider.addFilter(
          series ->
              series
                  .platform()
                  .toLowerCase(Locale.ROOT)
                  .contains(platformFilter.getValue().toLowerCase(Locale.ROOT)));
    }

    // Apply release year filter
    if (releaseYearFilter.getValue() != null) {
      dataProvider.addFilter(
          series ->
              series
                  .releaseYear()
                  .map(year -> year.getYear() == releaseYearFilter.getValue())
                  .orElse(false));
    }

    // Apply genre filter
    if (genreFilter.getValue() != null) {
      dataProvider.addFilter(series -> series.genre().contains(genreFilter.getValue()));
    }
  }

  private Set<Integer> getReleaseYears(List<Series> seriesList) {
    return seriesList.stream()
        .map(series -> series.releaseYear().map(LocalDate::getYear))
        .flatMap(Optional::stream)
        .collect(Collectors.toSet());
  }

  private Set<String> getAllGenres(List<Series> seriesList) {
    return seriesList.stream()
        .flatMap(series -> series.genre().stream())
        .collect(Collectors.toSet());
  }

  // Dialog for viewing Series details
  private void openSeriesDetailsDialog(Series series) {
    Dialog dialog = new Dialog();
    FormLayout formLayout = new FormLayout();

    formLayout.addFormItem(new TextField(Labels.TITLE, series.title()), Labels.TITLE);
    formLayout.addFormItem(new TextField(Labels.PLATFORM, series.platform()), Labels.PLATFORM);
    formLayout.addFormItem(
        new TextField(Labels.GENRES, String.join(", ", series.genre())), Labels.GENRES);
    formLayout.addFormItem(
        new TextField(
            Labels.RELEASE_YEAR, series.releaseYear().map(LocalDate::toString).orElse(Labels.NA)),
        Labels.RELEASE_YEAR);
    formLayout.addFormItem(
        new TextField(Labels.END_YEAR, series.endYear().map(LocalDate::toString).orElse("Ongoing")),
        Labels.END_YEAR);
    formLayout.addFormItem(new TextField(Labels.URL, series.url().orElse("")), Labels.URL);
    formLayout.addFormItem(
        new TextField(Labels.TAGS, series.tags().map(Map::toString).orElse("No tags")),
        Labels.TAGS);

    UnorderedList seasonList = new UnorderedList();
    for (Map.Entry<String, Integer> entry : series.seasons().entrySet()) {
      seasonList.add(new ListItem(entry.getKey() + ": " + entry.getValue()));
    }

    // add a section for the progress details
    Details details = new Details("Seasons", seasonList);
    details.setOpened(true);

    formLayout
        .getChildren()
        .filter(child -> child instanceof TextField)
        .map(child -> (TextField) child)
        .forEach(field -> field.setReadOnly(true));

    Button closeButton = new Button("Close", e -> dialog.close());
    dialog.add(formLayout, details, closeButton);
    dialog.open();
  }

  // Dialog for editing Series details
  private void openSeriesEditDialog(Series series) {
    Dialog dialog = new Dialog();
    FormLayout formLayout = new FormLayout();

    TextField titleField = new TextField(Labels.TITLE, series.title());
    TextField platformField = new TextField(Labels.PLATFORM, series.platform());
    TextField genreField = new TextField(Labels.GENRES_INPUT, String.join(", ", series.genre()));
    TextField releaseYearField =
        new TextField(
            Labels.RELEASE_YEAR, series.releaseYear().map(LocalDate::toString).orElse(""));
    TextField endYearField =
        new TextField(Labels.END_YEAR, series.endYear().map(LocalDate::toString).orElse(""));

    formLayout.add(titleField, platformField, genreField, releaseYearField, endYearField);

    for (Map.Entry<String, Integer> entry : series.seasons().entrySet()) {
      String season = entry.getKey();
      Integer totalEpisodes = entry.getValue();
      TextField seasonField = new TextField(season);
      seasonField.setValue(totalEpisodes.toString());
      formLayout.addFormItem(new HorizontalLayout(seasonField), season);
      seasonField.addValueChangeListener(
          e -> series.seasons().put(season, Integer.parseInt(e.getValue())));
    }

    Button saveButton =
        new Button(
            "Save",
            event -> {
              // Logic for saving the edited series
              seriesGrid.getDataProvider().refreshItem(series); // Refresh the item in the grid
              Notification.show("Series updated!");
              dialog.close();
            });

    Button cancelButton = new Button("Cancel", e -> dialog.close());

    dialog.add(formLayout, new HorizontalLayout(saveButton, cancelButton));
    dialog.open();
  }

  // Dialog for adding Series to WatchList
  private void openAddToWatchListDialog(Series series) {
    Dialog dialog = new Dialog();
    FormLayout formLayout = new FormLayout();

    // Fetch available WatchLists (you may have a service to get this)
    User user = userService.getLoggedInUser();
    List<WatchList> availableWatchLists = watchlistService.getWatchListsForUser(user);

    ComboBox<WatchList> watchListComboBox = new ComboBox<>("Select WatchList");
    watchListComboBox.setItems(availableWatchLists);
    watchListComboBox.setItemLabelGenerator(WatchList::getName); // Assuming WatchList has getName()

    var progressMap = new HashMap<String, Integer>();
    series.seasons().forEach((season, episodes) -> progressMap.put(season, 0));

    Button addToWatchListButton =
        new Button(
            "Add",
            event -> {
              WatchList selectedWatchList = watchListComboBox.getValue();
              if (selectedWatchList == null) {
                ViewNotifications.showErrorNotification("Please select a WatchList");
                return;
              }
              if (selectedWatchList.getItems().contains(series)) {
                ViewNotifications.showErrorNotification(
                    "Series already in WatchList: " + selectedWatchList.getName());
                return;
              }

              watchlistService.addMedia(selectedWatchList, series);
              progressService.createSeriesProgressForUser(user, series);

              ViewNotifications.showSuccessNotification(
                  "Series added to WatchList: " + selectedWatchList.getName());
              dialog.close();
            });

    Button cancelButton = new Button("Cancel", e -> dialog.close());

    formLayout.add(watchListComboBox);
    dialog.add(formLayout, new HorizontalLayout(addToWatchListButton, cancelButton));
    dialog.open();
  }
}
