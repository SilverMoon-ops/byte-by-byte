package sonus.app;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TextField;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ListCell;

import sonus.core.AudioEngine;
import sonus.core.FFmpegPlayerEngine;
import sonus.core.PlayerState;
import sonus.core.PlaylistManager;
import sonus.service.PlaylistStorageService;
import sonus.model.SavedPlaybackState;

import sonus.model.Song;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;

public class SonusApplication
        extends Application {

    private boolean seeking = false;

    private VBox queueSection;

    private final AudioEngine engine =
            new FFmpegPlayerEngine();

    private Slider progressSlider;

    private Slider volumeSlider;

    private Label currentTimeLabel;

    private Label totalTimeLabel;

    private Button playPauseButton;

    private StackPane vinylDiscAssembly;

    private double previousVolume = 1.0;

    private boolean muted = false;

    private Label songTitleLabel;

    private Label artistLabel;

    private ImageView albumArtView;

    private long lastSeekTimestamp = 0;

    private String currentViewMode = "Compact";// Tracks view rendering layout style

    private final java.util.Map<String, Image> artworkCache = new java.util.HashMap<>();
    private final Image defaultCover = new Image("https://placehold.co/100x100/222222/ffffff?text=Music"); // Fallback placeholder

    @Override
    public void start(Stage stage) {

        //====================
        // Playlist Manager
        //======================

        PlaylistManager playlistManager =
                new PlaylistManager();

        // =========================
        // Playlist Storage
        // =========================

        PlaylistStorageService
                playlistStorageService =
                new PlaylistStorageService();

        // =========================
        // Load Saved Playlist
        // =========================

        SavedPlaybackState state =
                playlistStorageService
                        .loadPlaybackState();

        List<Song> savedSongs =
                state.getPlaylist();

        List<Song> savedQueue =
                state.getQueue();

        for (Song song : savedSongs) {

            playlistManager.addSong(song);
        }

        // =========================
        // Playlist View Data
        // =========================

        ObservableList<Song> playlistItems =
                FXCollections.observableArrayList(
                        playlistManager.getSongs()
                );

        ObservableList<Song> queueItems =
                FXCollections.observableArrayList();

        queueItems.addAll(
                savedQueue
        );

        for (Song song : savedQueue) {

            playlistManager.addToQueue(
                    song
            );
        }

        // Filtered Playlist
        FilteredList<Song> filteredSongs =
                new FilteredList<>(
                        playlistItems,
                        song -> true
                );

        // NEW: Dynamic sorting layer wrapper attached over search filtering sequence
        SortedList<Song> sortedSongs = new SortedList<>(filteredSongs);

        // =========================
        // Playlist View
        // =========================
        ListView<Song> playlistView =
                new ListView<>(sortedSongs); // Now points to sortedSongs directly

        playlistView.setPrefWidth(700);

        playlistView.setMaxWidth(
                Double.MAX_VALUE
        );
        playlistView
                .getStyleClass()
                .add("playlist-view");

        StackPane playlistContainer = new StackPane(playlistView);

        ListView<Song> queueView =
                new ListView<>(queueItems);

        queueView.setPrefWidth(320);

        queueView.setPrefHeight(400);

        queueView.setMaxWidth(
                Double.MAX_VALUE
        );

        queueView
                .getStyleClass()
                .add("queue-view");

        queueView.setPlaceholder(

                new Label(
                        "No queued songs"
                )
        );

        // Safe Drag and Drop Configuration for the Queue List Cells
        queueView.setCellFactory(lv -> {
            ListCell<Song> cell = new ListCell<Song>() {
                @Override
                protected void updateItem(Song song, boolean empty) {
                    super.updateItem(song, empty);
                    if (empty || song == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Label textLabel = new Label(song.getTitle());
                        Label durationLabel = new Label(formatTime(song.getDuration()));
                        durationLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

                        HBox cellLayout = new HBox(textLabel, durationLabel);
                        HBox.setHgrow(textLabel, Priority.ALWAYS);
                        cellLayout.setAlignment(Pos.CENTER_LEFT);
                        setGraphic(cellLayout);
                        setText(null);
                    }
                }
            };

            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    // Store the index of the row being dragged
                    content.putString(String.valueOf(cell.getIndex()));
                    db.setContent(content);
                    event.consume();
                }
            });

            cell.setOnDragOver(event -> {
                if (event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIdx = Integer.parseInt(db.getString());
                    int droppedIdx = cell.getIndex();

                    if (draggedIdx >= 0 && draggedIdx < queueItems.size() && droppedIdx >= 0 && droppedIdx < queueItems.size() && draggedIdx != droppedIdx) {
                        Song draggedSong = queueItems.remove(draggedIdx);
                        queueItems.add(droppedIdx, draggedSong);

                        // Synchronize your underlying manager backend pipeline state
                        playlistManager.getQueue().clear();
                        playlistManager.getQueue().addAll(queueItems);

                        queueView.getSelectionModel().select(droppedIdx);
                        event.setDropCompleted(true);
                    } else {
                        event.setDropCompleted(false);
                    }
                }
                event.consume();
            });

            return cell;
        });

        ContextMenu playlistContextMenu =
                new ContextMenu();

        MenuItem playItem =
                new MenuItem(
                        "Play"
                );

        MenuItem queueSongItem =
                new MenuItem(
                        "Add To Queue"
                );

        MenuItem removeSongItem =
                new MenuItem(
                        "Remove Song"
                );

        MenuItem openLocationItem =
                new MenuItem(
                        "Open File Location"
                );

        MenuItem songInfoItem =
                new MenuItem(
                        "Song Info"
                );

        playlistContextMenu
                .getItems()
                .addAll(
                        playItem,
                        queueSongItem,
                        removeSongItem,
                        openLocationItem,
                        songInfoItem
                );

        // Add this right after playlistContextMenu.getItems().addAll(...)
        openLocationItem.setOnAction(event -> {
            Song selectedSong = playlistView.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                try {
                    File file = new File(selectedSong.getFilePath());
                    if (file.exists()) {
                        if (System.getProperty("os.name").toLowerCase().contains("win")) {
                            // Opens Windows Explorer and highlights the file directly
                            Runtime.getRuntime().exec("explorer.exe /select,\"" + file.getAbsolutePath() + "\"");
                        } else {
                            // Fallback for cross-platform support (opens parent folder)
                            java.awt.Desktop.getDesktop().open(file.getParentFile());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        songInfoItem.setOnAction(event -> {
            Song selectedSong = playlistView.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Song Information");
                alert.setHeaderText(selectedSong.getTitle());

                String info = "Artist: " + selectedSong.getArtist() + "\n" +
                        "Format: " + selectedSong.getExtension().toUpperCase() + "\n" +
                        "Duration: " + formatTime(selectedSong.getDuration()) + "\n" +
                        "Path: " + selectedSong.getFilePath();

                alert.setContentText(info);
                alert.showAndWait();
            }
        });

        playlistView.setContextMenu(
                playlistContextMenu
        );

        queueSongItem.setOnAction(event -> {

            Song selectedSong =
                    playlistView
                            .getSelectionModel()
                            .getSelectedItem();

            if (selectedSong == null) {
                return;
            }

            playlistManager.addToQueue(
                    selectedSong
            );

            queueItems.add(
                    selectedSong
            );
        });

        playItem.setOnAction(event -> {

            Song selectedSong =
                    playlistView
                            .getSelectionModel()
                            .getSelectedItem();

            if (selectedSong == null) {
                return;
            }

            try {

                engine.stop();

                engine.load(
                        selectedSong
                );

                playlistManager.setCurrentSong(
                        selectedSong
                );

                updateCurrentSongUI(

                        selectedSong,

                        playlistView,

                        songTitleLabel,

                        artistLabel,

                        albumArtView,

                        vinylDiscAssembly
                );

                engine.play();

                playPauseButton.setText(
                        "⏸"
                );

            } catch (Exception e) {

                e.printStackTrace();
            }

            System.out.println(
                    playlistView
                            .getSelectionModel()
                            .getSelectedItem()
            );
        });

        removeSongItem.setOnAction(event -> {

            Song selectedSong =
                    playlistView
                            .getSelectionModel()
                            .getSelectedItem();

            if (selectedSong == null) {
                return;
            }

            playlistManager.removeSong(
                    selectedSong
            );

            playlistItems.remove(
                    selectedSong
            );
        });

        ContextMenu queueContextMenu =
                new ContextMenu();

        MenuItem removeQueueItem =
                new MenuItem(
                        "Remove From Queue"
                );

        MenuItem clearQueueItem =
                new MenuItem(
                        "Clear Queue"
                );

        queueContextMenu.getItems().addAll(

                removeQueueItem,

                clearQueueItem
        );

        queueView.setContextMenu(
                queueContextMenu
        );

        /*queueView.setOnDragDetected(event -> {

            Song selectedSong =
                    queueView
                            .getSelectionModel()
                            .getSelectedItem();

            if (selectedSong == null) {
                return;
            }
        });*/

        // Search Bar
        TextField searchField =
                new TextField();

        searchField
                .getStyleClass()
                .add("search-field");

        searchField.setPromptText(
                "Search songs..."
        );

        // Search Filtering
        searchField.textProperty().addListener(

                (observable, oldValue, newValue) -> {

                    filteredSongs.setPredicate(song -> {

                        if (
                                newValue == null
                                        ||
                                        newValue.trim().isEmpty()
                        ) {

                            return true;
                        }

                        String search =
                                String.valueOf(newValue)
                                        .toLowerCase();

                        if (
                                song.getTitle()
                                        .toLowerCase()
                                        .contains(search)
                        ) {

                            return true;
                        }

                        return song.getArtist()
                                .toLowerCase()
                                .contains(search);
                    });
                }
        );

        // =====================================================================
        // Sort Dropdown Menu (VLC Style)
        // =====================================================================
        MenuButton sortButton = new MenuButton("Sort");

        sortButton
                .getStyleClass()
                .add("glass-button");

        MenuItem sortByTitle = new MenuItem("Title");
        MenuItem sortByArtist = new MenuItem("Artist");
        MenuItem sortByDuration = new MenuItem("Duration");
        MenuItem sortByRecentlyAdded = new MenuItem("Recently Added");

        sortButton.getItems().addAll(sortByTitle, sortByArtist, sortByDuration, sortByRecentlyAdded);

        sortByTitle.setOnAction(e -> {
            sortedSongs.setComparator(Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER));
            sortButton.setText("Sort: Title");
        });

        sortByArtist.setOnAction(e -> {
            sortedSongs.setComparator(Comparator.comparing(Song::getArtist, String.CASE_INSENSITIVE_ORDER));
            sortButton.setText("Sort: Artist");
        });

        sortByDuration.setOnAction(e -> {
            sortedSongs.setComparator(Comparator.comparingLong(Song::getDuration));
            sortButton.setText("Sort: Duration");
        });

        sortByRecentlyAdded.setOnAction(e -> {
            sortedSongs.setComparator(Comparator.comparingInt(playlistItems::indexOf).reversed());
            sortButton.setText("Sort: Recent");
        });

        // =====================================================================
        // View Dropdown Menu & Cell Factory Layout Switcher (VLC Style - 3 View Modes)
        // =====================================================================
        MenuButton viewButton = new MenuButton("Playlist View");

        viewButton
                .getStyleClass()
                .add("glass-button");

        MenuItem viewCompact = new MenuItem("Compact List");
        MenuItem viewDetailed = new MenuItem("Detailed Rows");
        MenuItem viewGrid = new MenuItem("Icon Grid");

        viewButton.getItems().addAll(viewCompact, viewDetailed, viewGrid);

        viewCompact.setOnAction(e -> {
            currentViewMode = "Compact";
            viewButton.setText("View: Compact");
            playlistContainer.getChildren().setAll(playlistView); // Restore standard list
            playlistView.refresh();
        });

        viewDetailed.setOnAction(e -> {
            currentViewMode = "Detailed";
            viewButton.setText("View: Detailed");
            playlistContainer.getChildren().setAll(playlistView); // Restore standard list
            playlistView.refresh();
        });

        viewGrid.setOnAction(e -> {
            currentViewMode = "Grid";
            viewButton.setText("View: Icon Grid");
            renderGridView(playlistView, playlistContainer, playlistManager, engine, songTitleLabel, artistLabel, albumArtView, vinylDiscAssembly, playPauseButton, progressSlider, currentTimeLabel);
        });

        // Auto-refresh the Grid Layout if user searches or sorts items while Grid view is active
        sortedSongs.addListener((javafx.collections.ListChangeListener<Song>) change -> {
            if ("Grid".equals(currentViewMode)) {
                renderGridView(playlistView, playlistContainer, playlistManager, engine, songTitleLabel, artistLabel, albumArtView, vinylDiscAssembly, playPauseButton, progressSlider, currentTimeLabel);
            }
        });

        playlistView.setCellFactory(lv -> new ListCell<Song>() {

            @Override
            protected void updateItem(
                    Song song,
                    boolean empty
            ) {

                super.updateItem(
                        song,
                        empty
                );

                if (
                        empty ||
                                song == null
                ) {

                    setText(null);

                    setGraphic(null);

                    return;
                }

                // =========================================
                // Compact View
                // =========================================

                if (
                        "Compact".equals(
                                currentViewMode
                        )
                ) {

                    Label textLabel =
                            new Label(
                                    song.getTitle()
                                            + " - "
                                            + song.getArtist()
                            );

                    textLabel.setStyle(
                            "-fx-text-fill: white;"
                    );

                    Label durationLabel =
                            new Label(
                                    formatTime(
                                            song.getDuration()
                                    )
                            );

                    durationLabel.setStyle(
                            "-fx-text-fill: #b0b7c3;"
                    );

                    HBox cellLayout =
                            new HBox(
                                    textLabel,
                                    durationLabel
                            );

                    HBox.setHgrow(
                            textLabel,
                            Priority.ALWAYS
                    );

                    cellLayout.setAlignment(
                            Pos.CENTER_LEFT
                    );

                    setGraphic(
                            cellLayout
                    );

                    setText(null);

                    return;
                }

                // =========================================
                // Detailed View
                // =========================================

                HBox cellLayout = new HBox(12);
                cellLayout.setAlignment(Pos.CENTER_LEFT);
                cellLayout.setPadding(new Insets(4, 0, 4, 0));

                // Create Row Thumbnail Image Container
                StackPane thumbContainer = new StackPane();
                thumbContainer.setPrefSize(40, 40);
                thumbContainer.setStyle("-fx-background-color: #333333; -fx-background-radius: 4px;");

                Label musicNote = new Label("🎵");
                musicNote.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");

                ImageView thumbView = new ImageView();

                // --- VERIFY / UPDATE THESE SIZES TO 40 ---
                thumbView.setFitWidth(40);
                thumbView.setFitHeight(40);
                thumbView.setPreserveRatio(true);

                // Round thumbnail cover edges slightly
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(40, 40);
                clip.setArcWidth(6);
                clip.setArcHeight(6);
                thumbView.setClip(clip);

                // Tag image view to verify async loading index integrity
                thumbView.getProperties().put("filePath", song.getFilePath());

                // --- ENSURE THIS CALL PASSES 40 ---
                lazyLoadArtwork(song.getFilePath(), thumbView, 40);

                thumbContainer.getChildren().addAll(musicNote, thumbView);

                // Text Section

                VBox textStack =
                        new VBox(
                                2
                        );

                Label titleLabel =
                        new Label(
                                song.getTitle()
                        );

                titleLabel.setStyle(
                        "-fx-font-weight: bold;" +
                                "-fx-font-size: 13px;" +
                                "-fx-text-fill: white;"
                );

                Label artistLabel =
                        new Label(
                                song.getArtist()
                        );

                artistLabel.setStyle(
                        "-fx-text-fill: #b0b7c3;" +
                                "-fx-font-size: 11px;"
                );

                textStack.getChildren().addAll(
                        titleLabel,
                        artistLabel
                );

                // Badges

                Label extLabel =
                        new Label(
                                song.getExtension()
                                        .toUpperCase()
                        );

                extLabel.setStyle(
                        "-fx-text-fill: white;" +
                                "-fx-background-color: #4a4f5e;" +
                                "-fx-padding: 2 6;" +
                                "-fx-background-radius: 4;" +
                                "-fx-font-size: 10px;" +
                                "-fx-font-weight: bold;"
                );

                Label durationLabel =
                        new Label(
                                formatTime(
                                        song.getDuration()
                                )
                        );

                durationLabel.setStyle(
                        "-fx-text-fill: white;" +
                                "-fx-background-color: #3a3f4d;" +
                                "-fx-padding: 2 6;" +
                                "-fx-background-radius: 4;" +
                                "-fx-font-size: 11px;"
                );

                HBox badgeSection =
                        new HBox(
                                8,
                                extLabel,
                                durationLabel
                        );

                badgeSection.setAlignment(
                        Pos.CENTER_RIGHT
                );

                HBox.setHgrow(
                        textStack,
                        Priority.ALWAYS
                );

                cellLayout.getChildren().addAll(
                        thumbContainer,
                        textStack,
                        badgeSection
                );

                setGraphic(
                        cellLayout
                );

                setText(null);
            }
        });

        // =========================
        // Album Artwork View
        // =========================
        Label nowPlayingLabel =
                new Label("Now Playing");

        songTitleLabel =
                new Label("No song selected");

        artistLabel =
                new Label("");

        albumArtView =
                new ImageView();

        javafx.scene.layout.Region vinylOuterBody =
                new javafx.scene.layout.Region();

        vinylOuterBody.setStyle(
                "-fx-background-color: #1c1c1c; " +
                        "-fx-background-radius: 50%;"
        );

        vinylOuterBody.setMaxSize(
                92,
                92
        );

        javafx.scene.layout.Region vinylGrooveRing =
                new javafx.scene.layout.Region();

        vinylGrooveRing.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: #2d2d2d; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-radius: 50%;"
        );

        vinylGrooveRing.setMaxSize(
                72,
                72
        );

        javafx.scene.layout.Region vinylCenterLabel =
                new javafx.scene.layout.Region();

        vinylCenterLabel.setStyle(
                "-fx-background-color: #cc3333; " +
                        "-fx-background-radius: 50%;"
        );

        vinylCenterLabel.setMaxSize(
                34,
                34
        );

        javafx.scene.layout.Region spindleHole =
                new javafx.scene.layout.Region();

        spindleHole.setStyle(
                "-fx-background-color: #fcfcfc; " +
                        "-fx-background-radius: 50%;"
        );

        spindleHole.setMaxSize(
                8,
                8
        );

        vinylDiscAssembly = new StackPane(
                vinylOuterBody,
                vinylGrooveRing,
                vinylCenterLabel,
                spindleHole
        );

        vinylDiscAssembly.setAlignment(
                Pos.CENTER
        );

        // =========================
        // Playlist Double Click
        // =========================
        playlistView.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2) {

                Song selectedSong =
                        playlistView
                                .getSelectionModel()
                                .getSelectedItem();

                if (selectedSong == null) {
                    return;
                }

                try {

                    engine.stop();

                    engine.load(selectedSong);

                    progressSlider.setValue(0);

                    currentTimeLabel.setText("00:00");

                    playlistManager.setCurrentSong(
                            selectedSong
                    );

                    updateCurrentSongUI(

                            selectedSong,

                            playlistView,

                            songTitleLabel,

                            artistLabel,

                            albumArtView,

                            vinylDiscAssembly
                    );

                    engine.play();

                    playPauseButton.setText("⏸");

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });

        // =========================
        // Auto Play Next Song
        // =========================
        engine.setOnSongFinished(() -> {

            Platform.runLater(() -> {

                try {

                    Song nextSong =
                            playlistManager.nextSong();

                    queueItems.remove(nextSong);

                    if (nextSong == null) {
                        return;
                    }

                    engine.stop();

                    engine.load(nextSong);

                    progressSlider.setValue(0);

                    currentTimeLabel.setText("00:00");

                    engine.play();

                    updateCurrentSongUI(

                            nextSong,

                            playlistView,

                            songTitleLabel,

                            artistLabel,

                            albumArtView,

                            vinylDiscAssembly
                    );

                } catch (Exception e) {

                    e.printStackTrace();
                }
            });
        });

        // =========================
        // Progress Slider
        // =========================
        progressSlider =
                new Slider();

        progressSlider.setMin(0);

        progressSlider.setMax(100);

        progressSlider.setValue(0);

        progressSlider.setPrefWidth(500);

        progressSlider.setOnMousePressed(event -> {
            seeking = true;
            progressSlider.setValueChanging(true); // Tells the UI an manual adjustment is happening
        });

        progressSlider.setOnMouseReleased(event -> {
            progressSlider.setValueChanging(false); // Manual adjustment finished

            double seekTime = progressSlider.getValue();
            engine.seek(seekTime);

            seeking = false;
        });
        // =========================
        // Time Labels
        // =========================
        currentTimeLabel =
                new Label("00:00");

        totalTimeLabel =
                new Label("00:00");

        // =========================
        // Playback Callback
        // =========================
        engine.setOnProgressUpdate(() -> {
            Platform.runLater(() -> {
                // 1. Never touch the slider value if the user is dragging it or seeking
                if (seeking || progressSlider.isValueChanging()) {
                    return;
                }

                double current = engine.getCurrentTime();
                double total = engine.getTotalDuration();

                // 2. Only update the Max bounds if it's a valid positive length
                // and has actually changed (prevents constant UI layout calculations)
                if (total > 0 && Math.abs(progressSlider.getMax() - total) > 0.1) {
                    progressSlider.setMax(total);
                }

                // 3. Update the slider position safely
                progressSlider.setValue(current);

                currentTimeLabel.setText(formatTime(current));
                totalTimeLabel.setText(formatTime(total));
            });
        });

        // =========================
        // Volume Slider
        // =========================
        Label volumeIcon = new Label("🔊");

        volumeSlider = new Slider(0, 100, 100);
        volumeSlider.setPrefWidth(180);

        volumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            double vol = newValue.doubleValue();
            engine.setVolume(vol);

            if (vol > 0) {
                muted = false;
                volumeIcon.setText("🔊");
            }
        });

        volumeIcon.setOnMouseClicked(event -> {
            if (!muted) {
                previousVolume = volumeSlider.getValue();
                volumeSlider.setValue(0);
                engine.setVolume(0);
                volumeIcon.setText("🔇");
                muted = true;
            } else {
                volumeSlider.setValue(previousVolume);
                engine.setVolume(previousVolume);
                volumeIcon.setText("🔊");
                muted = false;
            }
        });

        HBox volumeSection = new HBox(10, volumeIcon, volumeSlider);
        volumeSection.setAlignment(Pos.CENTER);

        // =====================================================================
        // Playback Speed Selector (YouTube / VLC Style)
        // =====================================================================
        MenuButton speedButton = new MenuButton("Speed:1.0x");
        speedButton.setPrefHeight(40); // Matches the height of your playback control buttons

        MenuItem speed05 = new MenuItem("0.5x (Slow)");
        MenuItem speed10 = new MenuItem("1.0x (Normal)");
        MenuItem speed125 = new MenuItem("1.25x");
        MenuItem speed15 = new MenuItem("1.5x");
        MenuItem speed20 = new MenuItem("2.0x (Fast)");

        speedButton.getItems().addAll(speed05, speed10, speed125, speed15, speed20);

        // Connect menu clicks directly to your updated AudioEngine interface
        speed05.setOnAction(e -> {
            engine.setSpeed(0.5);
            speedButton.setText("Speed:0.5x");
        });
        speed10.setOnAction(e -> {
            engine.setSpeed(1.0);
            speedButton.setText("Speed:1.0x");
        });
        speed125.setOnAction(e -> {
            engine.setSpeed(1.25);
            speedButton.setText("Speed:1.25x");
        });
        speed15.setOnAction(e -> {
            engine.setSpeed(1.5);
            speedButton.setText("Speed:1.5x");
        });
        speed20.setOnAction(e -> {
            engine.setSpeed(2.0);
            speedButton.setText("Speed:2.0x");
        });

        // =========================
        // Toolbar Buttons
        // =========================
        Button addSongButton =
                new Button("Add Song");

        Button addMultipleButton =
                new Button("Add Multiple");

        Button addFolderButton =
                new Button("Add Folder");

        Button removeSongButton =
                new Button("Remove Song");

        Button clearPlaylistButton =
                new Button("Clear Playlist");

        // =========================
        // Add Single Song
        // =========================
        addSongButton.setOnAction(event -> {

            FileChooser fileChooser =
                    new FileChooser();

            fileChooser.setTitle(
                    "Select Audio File"
            );

            File selectedFile =
                    fileChooser.showOpenDialog(stage);

            if (selectedFile == null) {
                return;
            }

            try {

                Song song =
                        createSongFromFile(
                                selectedFile
                        );

                if (song == null) {
                    return;
                }

                playlistManager.addSong(song);

                playlistItems.add(song);

            } catch (Exception e) {

                e.printStackTrace();
            }
        });

        // =========================
        // Add Multiple Songs
        // ===========================
        addMultipleButton.setOnAction(event -> {

            FileChooser fileChooser =
                    new FileChooser();

            fileChooser.setTitle(
                    "Select Audio Files"
            );

            List<File> selectedFiles =
                    fileChooser.showOpenMultipleDialog(stage);

            if (selectedFiles == null) {
                return;
            }

            for (File file : selectedFiles) {

                try {

                    Song song =
                            createSongFromFile(
                                    file
                            );

                    if (song == null) {
                        continue;
                    }

                    playlistManager.addSong(song);

                    playlistItems.add(song);

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });

        // =========================
        // Add Folder
        // =========================
        addFolderButton.setOnAction(event -> {

            DirectoryChooser directoryChooser =
                    new DirectoryChooser();

            directoryChooser.setTitle(
                    "Select Music Folder"
            );

            File selectedFolder =
                    directoryChooser.showDialog(stage);

            if (selectedFolder == null) {
                return;
            }

            File[] files =
                    selectedFolder.listFiles();

            if (files == null) {
                return;
            }

            for (File file : files) {

                if (!file.isFile()) {
                    continue;
                }

                try {

                    Song song =
                            createSongFromFile(
                                    file
                            );

                    if (song == null) {
                        continue;
                    }

                    playlistManager.addSong(song);

                    playlistItems.add(song);

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            if (
                    playlistManager.getCurrentSong()
                            == null
                            &&
                            !playlistItems.isEmpty()
            ) {

                Song firstSong =
                        playlistItems.get(0);

                playlistManager.setCurrentSong(
                        firstSong
                );

                updateCurrentSongUI(

                        firstSong,

                        playlistView,

                        songTitleLabel,

                        artistLabel,

                        albumArtView,

                        vinylDiscAssembly
                );
                try {

                    engine.load(firstSong);

                    engine.play();

                    playPauseButton.setText("⏸");

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });

        // =========================
        // Control Buttons
        // =========================
        Button shuffleButton =
                new Button("🔀");

        Button repeatButton =
                new Button("➡");

        Button previousButton =
                new Button("⏮");

        playPauseButton =
                new Button("▶");

        Button stopButton =
                new Button("⏹");

        Button nextButton =
                new Button("⏭");

        Button queueButton =
                new Button("Queue");

        Button removeQueueButton =
                new Button("Remove Queue");

        Button clearQueueButton =
                new Button("Clear Queue");

        previousButton.setPrefSize(50, 40);

        playPauseButton.setPrefSize(50, 40);

        stopButton.setPrefSize(50, 40);

        nextButton.setPrefSize(50, 40);

        shuffleButton.setPrefSize(50, 40);

        repeatButton.setPrefSize(50, 40);

        queueButton.setPrefSize(70, 40);

        removeQueueButton.setPrefSize(120, 40);

        clearQueueButton.setPrefSize(120, 40);

        // =========================
        // Play / Pause Logic
        // =========================
        playPauseButton.setOnAction(event -> {

            try {

                if (
                        engine.getState()
                                == PlayerState.PLAYING
                ) {

                    engine.pause();

                    playPauseButton.setText("▶");

                    return;
                }

                if (
                        engine.getState()
                                == PlayerState.STOPPED
                ) {

                    Song currentSong =
                            playlistManager.getCurrentSong();

                    if (currentSong == null) {

                        currentSong =
                                playlistView
                                        .getSelectionModel()
                                        .getSelectedItem();
                    }

                    if (currentSong == null) {
                        return;
                    }

                    engine.load(currentSong);

                    progressSlider.setValue(0);

                    currentTimeLabel.setText("00:00");

                    engine.setVolume(
                            volumeSlider.getValue()
                    );

                    updateCurrentSongUI(

                            currentSong,

                            playlistView,

                            songTitleLabel,

                            artistLabel,

                            albumArtView,

                            vinylDiscAssembly
                    );
                }

                engine.play();

                playPauseButton.setText("⏸");

            } catch (Exception e) {

                e.printStackTrace();
            }
        });

        // =========================
        // Stop Logic
        // =========================
        stopButton.setOnAction(event -> {
            try {
                engine.stop();
                playPauseButton.setText("▶");
                progressSlider.setValue(0);
                currentTimeLabel.setText("00:00");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // =========================
        // Next Song
        // =========================
        nextButton.setOnAction(event -> {

            try {

                Song nextSong =
                        playlistManager.nextSong();

                queueItems.remove(nextSong);

                engine.stop();

                engine.load(nextSong);

                progressSlider.setValue(0);

                currentTimeLabel.setText("00:00");

                engine.setVolume(volumeSlider.getValue());

                engine.play();

                updateCurrentSongUI(

                        nextSong,

                        playlistView,

                        songTitleLabel,

                        artistLabel,

                        albumArtView,

                        vinylDiscAssembly
                );

                playPauseButton.setText("⏸");

            } catch (Exception e) {

                System.out.println(
                        "No next song"
                );
                e.printStackTrace();
            }
        });

        // =========================
        // Previous Song
        // =========================
        previousButton.setOnAction(event -> {

            try {

                Song previousSong =
                        playlistManager.previousSong();

                engine.stop();

                engine.load(previousSong);

                progressSlider.setValue(0);

                currentTimeLabel.setText("00:00");

                engine.setVolume(volumeSlider.getValue());

                engine.play();

                updateCurrentSongUI(

                        previousSong,

                        playlistView,

                        songTitleLabel,

                        artistLabel,

                        albumArtView,

                        vinylDiscAssembly
                );

                playPauseButton.setText("⏸");

            } catch (Exception e) {

                System.out.println(
                        "No previous song"
                );
                e.printStackTrace();
            }
        });

        // =========================
        // Shuffle Toggle
        // =========================
        shuffleButton.setOnAction(event -> {

            if (
                    playlistManager
                            .isShuffleEnabled()
            ) {

                playlistManager
                        .disableShuffle();

                shuffleButton.setText("🔀");

            } else {

                playlistManager
                        .enableShuffle();

                shuffleButton.setText("🟢🔀");
            }
        });

        // =========================
        // Repeat Toggle
        // =========================
        repeatButton.setOnAction(event -> {

            if (
                    !playlistManager
                            .isRepeatPlaylist()
                            &&
                            !playlistManager
                                    .isRepeatSingleSong()
            ) {

                playlistManager
                        .setRepeatPlaylist(true);

                repeatButton.setText("🔁");

                return;
            }

            if (
                    playlistManager
                            .isRepeatPlaylist()
            ) {

                playlistManager
                        .setRepeatPlaylist(false);

                playlistManager
                        .setRepeatSingleSong(true);

                repeatButton.setText("🔂");

                return;
            }

            playlistManager
                    .setRepeatSingleSong(false);

            repeatButton.setText("➡");
        });

        // =========================
        // Remove Selected Song
        // =========================
        removeSongButton.setOnAction(event -> {
            Song selectedSong = playlistView.getSelectionModel().getSelectedItem();

            if (selectedSong == null) {
                return;
            }

            if (selectedSong.equals(playlistManager.getCurrentSong())) {
                try {
                    engine.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            playlistManager.removeSong(selectedSong);
            playlistItems.remove(selectedSong);
        });

        queueButton.setOnAction(event -> {

            boolean visible =
                    queueSection.isVisible();

            queueSection.setVisible(
                    !visible
            );

            queueSection.setManaged(
                    !visible
            );
        });

        removeQueueButton.setOnAction(event -> {

            Song selectedSong =
                    queueView
                            .getSelectionModel()
                            .getSelectedItem();

            if (selectedSong == null) {
                return;
            }

            playlistManager.removeFromQueue(
                    selectedSong
            );

            queueItems.remove(
                    selectedSong
            );
        });

        clearQueueButton.setOnAction(event -> {

            playlistManager.clearQueue();

            queueItems.clear();
        });

        queueView.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2) {

                Song selectedSong =
                        queueView
                                .getSelectionModel()
                                .getSelectedItem();

                if (selectedSong == null) {
                    return;
                }

                playlistManager.removeFromQueue(
                        selectedSong
                );

                queueItems.remove(
                        selectedSong
                );
            }
        });

        // =========================
        // Clear Playlist
        // =========================
        clearPlaylistButton.setOnAction(event -> {

            try {

                engine.stop();

            } catch (Exception e) {

                e.printStackTrace();
            }

            playlistManager.clear();

            playlistItems.clear();

            songTitleLabel.setText(
                    "No song selected"
            );

            artistLabel.setText("");

            currentTimeLabel.setText("00:00");

            totalTimeLabel.setText("00:00");

            progressSlider.setValue(0);

            playPauseButton.setText("▶");

            albumArtView.setImage(null);

            vinylDiscAssembly.setVisible(true);

            engine.setSpeed(1.0);
            speedButton.setText("Speed: 1.0x");
        });


        // =====================================================================
        // 1. Sleek Left Sidebar (Reduced Width Rail)
        // =====================================================================
        VBox sidebar =
                new VBox(
                        12
                );

        sidebar.setPrefWidth(145);

        sidebar.setPadding(
                new Insets(
                        20,
                        10,
                        20,
                        10
                )
        );

        sidebar.setAlignment(
                Pos.TOP_LEFT
        );

        Label libraryHeader =
                new Label(
                        "MEDIA"
                );

        Label queueHeader =
                new Label(
                        "UTILITIES"
                );

        addSongButton.setMaxWidth(
                Double.MAX_VALUE
        );

        addMultipleButton.setMaxWidth(
                Double.MAX_VALUE
        );

        addFolderButton.setMaxWidth(
                Double.MAX_VALUE
        );

        removeSongButton.setMaxWidth(
                Double.MAX_VALUE
        );

        clearPlaylistButton.setMaxWidth(
                Double.MAX_VALUE
        );

        queueButton.setMaxWidth(
                Double.MAX_VALUE
        );

        sidebar.getChildren()
                .addAll(
                        libraryHeader,
                        addSongButton,
                        addMultipleButton,
                        addFolderButton,
                        new Label(""),
                        queueHeader,
                        queueButton,
                        removeSongButton,
                        clearPlaylistButton
                );


        // =====================================================================
        // 2. Now Playing Display
        // =====================================================================
        nowPlayingLabel.setText(
                "NOW PLAYING"
        );

        albumArtView.setFitWidth(
                110
        );

        albumArtView.setFitHeight(
                110
        );

        albumArtView.setPreserveRatio(
                true
        );

        javafx.scene.shape.Rectangle artClip =
                new javafx.scene.shape.Rectangle(
                        110,
                        110
                );

        artClip.setArcWidth(
                12
        );

        artClip.setArcHeight(
                12
        );

        albumArtView.setClip(
                artClip
        );

        StackPane albumArtContainer =
                new StackPane(
                        vinylDiscAssembly,
                        albumArtView
                );

        albumArtContainer.setStyle(
                "-fx-background-color: #fcfcfc; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-background-radius: 6px;"
        );

        albumArtContainer.setPrefSize(
                110,
                110
        );

        albumArtContainer.setMinWidth(
                110
        );

        albumArtContainer.setMinHeight(
                110
        );

        albumArtContainer.setAlignment(
                Pos.CENTER
        );

        VBox metadataTextSection =
                new VBox(
                        4,
                        nowPlayingLabel,
                        songTitleLabel,
                        artistLabel
                );
        metadataTextSection.setAlignment(Pos.CENTER_LEFT);

        HBox nowPlayingHeader =
                new HBox(
                        15,
                        albumArtContainer,
                        metadataTextSection
                );

        nowPlayingHeader.setAlignment(
                Pos.CENTER_LEFT
        );

        nowPlayingHeader
                .getStyleClass()
                .add("card");

        nowPlayingHeader.setPadding(
                new Insets(
                        0,
                        0,
                        10,
                        0
                )
        );


        // =====================================================================
        // 3. Center Content Area (Playlist & Dynamic Queue Panels)
        // =====================================================================
        HBox searchSortRow = new HBox(10, searchField, sortButton, viewButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchSortRow.setAlignment(Pos.CENTER);

        VBox playlistSection =
                new VBox(
                        12,
                        nowPlayingHeader,
                        searchSortRow,
                        playlistContainer
                );

        VBox.setVgrow(
                playlistContainer,
                Priority.ALWAYS
        );

        Label queueLabel =
                new Label(
                        "QUEUE"
                );

        removeQueueButton.setPrefSize(100, 30);

        clearQueueButton.setPrefSize(100, 30);

        HBox queueControls =
                new HBox(
                        8,
                        removeQueueButton,
                        clearQueueButton
                );

        queueControls.setAlignment(
                Pos.CENTER
        );
        queueControls.setPadding(new Insets(5, 0, 0, 0));

        queueSection =
                new VBox(
                        10,
                        queueLabel,
                        queueView,
                        queueControls
                );

        queueSection.setPrefWidth(280);
        queueSection.setMinWidth(260);
        queueSection.setMaxWidth(320);
        queueSection.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(
                queueView,
                Priority.NEVER
        );

        queueSection.setVisible(false);

        queueSection.setManaged(false);

        HBox centerSection =
                new HBox(
                        15,
                        playlistSection,
                        queueSection
                );

        HBox.setHgrow(
                playlistSection,
                Priority.ALWAYS
        );

        centerSection.setPadding(
                new Insets(
                        15,
                        15,
                        15,
                        0
                )
        );


        // =====================================================================
        // 4. Unified Bottom Control Bar Setup
        // =====================================================================
        progressSlider.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                progressSlider,
                Priority.ALWAYS
        );

        HBox progressSection =
                new HBox(
                        10,
                        currentTimeLabel,
                        progressSlider,
                        totalTimeLabel
                );

        progressSection.setAlignment(
                Pos.CENTER
        );

        HBox playbackControls =
                new HBox(
                        12,
                        shuffleButton,
                        previousButton,
                        playPauseButton,
                        stopButton,
                        nextButton,
                        repeatButton
                );

        playbackControls.setAlignment(
                Pos.CENTER
        );

        HBox controlsRow =
                new HBox(
                        20,
                        playbackControls,
                        speedButton,
                        volumeSection
                );

        controlsRow.setAlignment(
                Pos.CENTER
        );

        VBox bottomSection =
                new VBox(
                        10,
                        progressSection,
                        controlsRow
                );

        bottomSection.setAlignment(
                Pos.CENTER
        );

        bottomSection.setPadding(
                new Insets(12)
        );


        // =====================================================================
        // 5. Master Root Frame Assembly
        // =====================================================================
        BorderPane root =
                new BorderPane();

        root.setLeft(
                sidebar
        );

        root.setCenter(
                centerSection
        );

        root.setBottom(
                bottomSection
        );

        // =========================
        // Scene Setup
        // =========================
        Scene scene =
                new Scene(
                        root,
                        900,
                        600
                );

        scene.getStylesheets().add(
                getClass()
                        .getResource("/css/sonus.css")
                        .toExternalForm()
        );

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // Only toggle playback if the search bar isn't being typed into
            if (event.getCode() == KeyCode.SPACE && !searchField.isFocused()) {
                playPauseButton.fire();
                event.consume();
            }
            // Delete items conditionally based on which list is currently active
            else if (event.getCode() == KeyCode.DELETE && !searchField.isFocused()) {
                if (queueView.isFocused()) {
                    // Trigger your remove from queue logic/button
                    removeQueueButton.fire();
                } else {
                    removeSongButton.fire();
                }
                event.consume();
            }
        });

// 2. Map Ctrl/Cmd combinations directly to global system accelerators
        scene.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(KeyCode.O, javafx.scene.input.KeyCombination.SHORTCUT_DOWN),
                () -> Platform.runLater(addSongButton::fire));

        scene.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(KeyCode.F, javafx.scene.input.KeyCombination.SHORTCUT_DOWN),
                () -> Platform.runLater(searchField::requestFocus));

        scene.getAccelerators().put(new javafx.scene.input.KeyCodeCombination(KeyCode.Q, javafx.scene.input.KeyCombination.SHORTCUT_DOWN),
                () -> Platform.runLater(this::toggleQueueVisibility)); // toggles queue visibility state

        // =========================
        // Drag Over
        // =========================
        scene.setOnDragOver(event -> {

            Dragboard dragboard =
                    event.getDragboard();

            if (
                    dragboard.hasFiles()
            ) {

                event.acceptTransferModes(
                        TransferMode.COPY
                );
            }

            event.consume();
        });

        // =========================
        // Drag Dropped
        // =========================
        scene.setOnDragDropped(event -> {

            Dragboard dragboard =
                    event.getDragboard();

            boolean success = false;

            if (
                    dragboard.hasFiles()
            ) {

                success = true;

                for (
                        File file
                        : dragboard.getFiles()
                ) {

                    if (file.isDirectory()) {

                        File[] files =
                                file.listFiles();

                        if (files == null) {
                            continue;
                        }

                        for (File innerFile : files) {

                            if (!innerFile.isFile()) {
                                continue;
                            }

                            Song song =
                                    createSongFromFile(
                                            innerFile
                                    );

                            if (song == null) {
                                continue;
                            }

                            playlistManager.addSong(
                                    song
                            );

                            playlistItems.add(
                                    song
                            );
                        }
                    }
                    else {

                        Song song =
                                createSongFromFile(
                                        file
                                );

                        if (song == null) {
                            continue;
                        }

                        playlistManager.addSong(
                                song
                        );

                        playlistItems.add(
                                song
                        );
                    }
                }

                if (
                        playlistManager.getCurrentSong()
                                == null
                                &&
                                !playlistItems.isEmpty()
                ) {

                    Song firstSong =
                            playlistItems.get(0);

                    playlistManager.setCurrentSong(
                            firstSong
                    );

                    updateCurrentSongUI(

                            firstSong,

                            playlistView,

                            songTitleLabel,

                            artistLabel,

                            albumArtView,

                            vinylDiscAssembly
                    );
                }
            }

            event.setDropCompleted(success);

            event.consume();
        });

        stage.setTitle("Sonus");

        stage.setScene(scene);

        stage.show();

        // =========================
        // Save Playlist On Exit
        // =========================
        stage.setOnCloseRequest(event -> {

            playlistStorageService
                    .savePlaybackState(

                            new ArrayList<>(
                                    playlistItems
                            ),

                            new ArrayList<>(
                                    queueItems
                            )
                    );
        });
    }

    private void toggleQueueVisibility() {
        boolean visible = queueSection.isVisible();
        queueSection.setVisible(!visible);
        queueSection.setManaged(!visible);
    }

    // =====================================================================
    // Sync Current Song UI & Handle Art Overlays Dynamically
    // =====================================================================
    private void updateCurrentSongUI(

            Song song,

            ListView<Song> playlistView,

            Label songTitleLabel,

            Label artistLabel,

            ImageView albumArtView,

            StackPane vinylDiscAssembly
    ) {

        if (song == null) {
            return;
        }

        songTitleLabel.setText(
                song.getTitle()
        );

        artistLabel.setText(
                song.getArtist()
        );

        playlistView
                .getSelectionModel()
                .select(song);

        playlistView.scrollTo(song);

        try {

            AudioFile audioFile =
                    AudioFileIO.read(
                            new File(
                                    song.getFilePath()
                            )
                    );

            Tag tag =
                    audioFile.getTag();

            if (
                    tag != null
            ) {

                Artwork artwork =
                        tag.getFirstArtwork();

                if (
                        artwork != null
                                &&
                                artwork.getBinaryData() != null
                ) {

                    Image image =
                            new Image(
                                    new ByteArrayInputStream(
                                            artwork.getBinaryData()
                                    )
                            );

                    albumArtView.setImage(
                            image
                    );

                    vinylDiscAssembly.setVisible(
                            false
                    );

                    return;
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        albumArtView.setImage(
                null
        );

        vinylDiscAssembly.setVisible(
                true
        );
    }

    // =========================
    // Create Song From File
    // =========================
    private Song createSongFromFile(
            File file
    ) {

        try {

            AudioFile audioFile =
                    AudioFileIO.read(file);

            Tag tag =
                    audioFile.getTag();

            String title =
                    tag.getFirst(
                            FieldKey.TITLE
                    );

            String artist =
                    tag.getFirst(
                            FieldKey.ARTIST
                    );

            long duration =
                    audioFile
                            .getAudioHeader()
                            .getTrackLength();

            if (
                    title == null
                            ||
                            title.isBlank()
            ) {

                String fileName =
                        file.getName();

                int dotIndex =
                        fileName.lastIndexOf(".");

                if (dotIndex > 0) {

                    fileName =
                            fileName.substring(
                                    0,
                                    dotIndex
                            );
                }

                title = fileName;
            }

            if (
                    artist == null
                            ||
                            artist.isBlank()
            ) {

                artist =
                        "Unknown Artist";
            }

            String extension =
                    file.getName()
                            .substring(
                                    file.getName()
                                            .lastIndexOf(".") + 1
                            );

            return new Song(

                    file.getAbsolutePath(),

                    title,

                    artist,

                    extension,

                    duration
            );

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    private String formatTime(double seconds) {
        int totalSeconds = (int) Math.round(seconds); //  Properly rounds 197.9 to 198
        int minutes = totalSeconds / 60;
        int remainingSeconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    // =====================================================================
    // Lazy Load Artwork Asynchronously (Prevents list scrolling lag)
    // =====================================================================
    private void lazyLoadArtwork(String filePath, ImageView imageView, double size) {
        // 1. Generate size-specific key
        String cacheKey = filePath + "_" + (int) size;

        // 2. Check cache using the new key
        if (artworkCache.containsKey(cacheKey)) {
            imageView.setImage(artworkCache.get(cacheKey));
            return;
        }

        imageView.setImage(null); // Clear image view first for reused cells

        new Thread(() -> {
            try {
                org.jaudiotagger.audio.AudioFile audioFile = org.jaudiotagger.audio.AudioFileIO.read(new File(filePath));
                org.jaudiotagger.tag.Tag tag = audioFile.getTag();
                if (tag != null) {
                    org.jaudiotagger.tag.images.Artwork artwork = tag.getFirstArtwork();
                    if (artwork != null && artwork.getBinaryData() != null) {
                        Image image = new Image(new java.io.ByteArrayInputStream(artwork.getBinaryData()), size, size, true, true);

                        // 3. Store valid image with the new key
                        artworkCache.put(cacheKey, image);

                        Platform.runLater(() -> {
                            // Verify image container is still expecting this specific file track
                            if (filePath.equals(imageView.getProperties().get("filePath"))) {
                                imageView.setImage(image);
                            }
                        });
                        return;
                    }
                }
            } catch (Exception e) {
                // Read exception handled gracefully, fallback used
            }
            // 4. Store null fallback with the new key
            artworkCache.put(cacheKey, null);
        }).start();
    }

    // =====================================================================
    // Dynamic VLC Grid UI Builder Engine
    // =====================================================================
    private void renderGridView(ListView<Song> playlistView, StackPane playlistContainer, PlaylistManager playlistManager, AudioEngine engine, Label songTitleLabel, Label artistLabel, ImageView albumArtView, StackPane vinylDiscAssembly, Button playPauseButton, Slider progressSlider, Label currentTimeLabel) {
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        javafx.scene.layout.TilePane tilePane = new javafx.scene.layout.TilePane();
        tilePane.setHgap(15);
        tilePane.setVgap(15);
        tilePane.setPadding(new Insets(12));

        // FIX 1: Center the grid rows inside the window (Spotify/Apple Music style)
        tilePane.setAlignment(Pos.TOP_CENTER);

        for (Song song : playlistView.getItems()) {
            VBox card = new VBox(8);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(10, 8, 10, 8)); // Denser vertical & horizontal padding
            card.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 8px; -fx-cursor: hand;");

            // FIX 2: Shrink card width boundaries from 150 to 140 for a tighter, denser feel
            card.setPrefWidth(140);
            card.setMaxWidth(140);

            StackPane artHolder = new StackPane();
            artHolder.setPrefSize(140, 140);
            artHolder.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 6px;");

            Label fallbackIcon = new Label("🎵");
            fallbackIcon.setStyle("-fx-text-fill: #444444; -fx-font-size: 28px;");

            ImageView cardArt = new ImageView();
            cardArt.setFitWidth(140);
            cardArt.setFitHeight(140);
            cardArt.setPreserveRatio(true);

            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(140, 140);
            clip.setArcWidth(8);
            clip.setArcHeight(8);
            cardArt.setClip(clip);

            cardArt.getProperties().put("filePath", song.getFilePath());
            lazyLoadArtwork(song.getFilePath(), cardArt, 140);

            artHolder.getChildren().addAll(fallbackIcon, cardArt);

            // FIX 3: Shrink text boundary labels to match the narrow card architecture
            Label title = new Label(song.getTitle());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: white;");
            title.setAlignment(Pos.CENTER);
            title.setMaxWidth(130);

            Label artist = new Label(song.getArtist());
            artist.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaaaaa;");
            artist.setAlignment(Pos.CENTER);
            artist.setMaxWidth(130);

            card.getChildren().addAll(artHolder, title, artist);

            // Wire individual card mouse selection and playback actions
            card.setOnMouseClicked(event -> {
                playlistView.getSelectionModel().select(song);
                if (event.getClickCount() == 2) {
                    try {
                        engine.stop();
                        engine.load(song);
                        progressSlider.setValue(0);
                        currentTimeLabel.setText("00:00");
                        playlistManager.setCurrentSong(song);
                        updateCurrentSongUI(song, playlistView, songTitleLabel, artistLabel, albumArtView, vinylDiscAssembly);
                        engine.play();
                        playPauseButton.setText("⏸");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            tilePane.getChildren().add(card);
        }

        scrollPane.setContent(tilePane);
        playlistContainer.getChildren().setAll(scrollPane);
    }



    public static void main(String[] args) {

        launch(args);
    }
}