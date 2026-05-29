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

    private String currentViewMode = "Compact"; // Tracks view rendering layout style

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

        ListView<Song> queueView =
                new ListView<>(queueItems);

        queueView.setPrefWidth(250);

        queueView.setPrefHeight(450);

        queueView.setMaxWidth(
                Double.MAX_VALUE
        );

        queueView.setPlaceholder(

                new Label(
                        "No queued songs"
                )
        );

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

        queueView.setOnDragDetected(event -> {

            Song selectedSong =
                    queueView
                            .getSelectionModel()
                            .getSelectedItem();

            if (selectedSong == null) {
                return;
            }
        });

        // Search Bar
        TextField searchField =
                new TextField();

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
        MenuButton sortButton = new MenuButton("Sort ▼");

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
        // View Dropdown Menu & Cell Factory Layout Switcher (VLC Style)
        // =====================================================================
        MenuButton viewButton = new MenuButton("Playlist View");

        MenuItem viewCompact = new MenuItem("Compact List");
        MenuItem viewDetailed = new MenuItem("Detailed Rows");

        viewButton.getItems().addAll(viewCompact, viewDetailed);

        viewCompact.setOnAction(e -> {
            currentViewMode = "Compact";
            viewButton.setText("View: Compact");
            playlistView.refresh();
        });

        viewDetailed.setOnAction(e -> {
            currentViewMode = "Detailed";
            viewButton.setText("View: Detailed");
            playlistView.refresh();
        });

        playlistView.setCellFactory(lv -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);

                if (empty || song == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if ("Compact".equals(currentViewMode)) {
                        Label textLabel = new Label(song.getTitle() + " - " + song.getArtist());
                        Label durationLabel = new Label(formatTime(song.getDuration()));
                        durationLabel.setStyle("-fx-text-fill: #888888;");

                        HBox cellLayout = new HBox(textLabel, durationLabel);
                        HBox.setHgrow(textLabel, Priority.ALWAYS);
                        cellLayout.setAlignment(Pos.CENTER_LEFT);

                        setGraphic(cellLayout);
                        setText(null);
                    } else {
                        VBox textStack = new VBox(2);
                        Label titleLabel = new Label(song.getTitle());
                        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                        Label artistLabel = new Label(song.getArtist());
                        artistLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
                        textStack.getChildren().addAll(titleLabel, artistLabel);

                        Label extLabel = new Label(song.getExtension().toUpperCase());
                        extLabel.setStyle("-fx-text-fill: #ffffff; -fx-background-color: #555555; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;");

                        Label durationLabel = new Label(formatTime(song.getDuration()));
                        durationLabel.setStyle("-fx-text-fill: #333333; -fx-background-color: #e0e0e0; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 11px;");

                        HBox badgeSection = new HBox(8, extLabel, durationLabel);
                        badgeSection.setAlignment(Pos.CENTER_RIGHT);

                        HBox cellLayout = new HBox(textStack, badgeSection);
                        HBox.setHgrow(textStack, Priority.ALWAYS);
                        cellLayout.setAlignment(Pos.CENTER_LEFT);
                        cellLayout.setPadding(new Insets(4, 0, 4, 0));

                        setGraphic(cellLayout);
                        setText(null);
                    }
                }
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
        });

        progressSlider.setOnMouseReleased(event -> {

            double seekTime =
                    progressSlider.getValue();

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

                double current =
                        engine.getCurrentTime();

                double total =
                        engine.getTotalDuration();

                progressSlider.setMax(total);

                if (!seeking) {

                    progressSlider.setValue(current);
                }

                currentTimeLabel.setText(
                        formatTime(current)
                );

                totalTimeLabel.setText(
                        formatTime(total)
                );
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
                        playlistView
                );

        VBox.setVgrow(
                playlistView,
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
                        10,
                        removeQueueButton,
                        clearQueueButton
                );

        queueControls.setAlignment(
                Pos.CENTER
        );

        queueSection =
                new VBox(
                        10,
                        queueLabel,
                        queueView,
                        queueControls
                );

        queueSection.setPrefWidth(250);

        VBox.setVgrow(
                queueView,
                Priority.ALWAYS
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

        // =====================================================================
        // FIXED: Keyboard Event Capture System (Using Event Filter)
        // =====================================================================
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {

            // If the text box has focus, allow typing normally without intercepting
            if (searchField.isFocused()) {
                return;
            }

            if (event.getCode() == KeyCode.SPACE) {
                playPauseButton.fire();
                event.consume(); // Intercept and clear to stop slider/button conflicts
            }
            else if (event.getCode() == KeyCode.RIGHT) {
                nextButton.fire();
                event.consume();
            }
            else if (event.getCode() == KeyCode.LEFT) {
                previousButton.fire();
                event.consume();
            }
            else if (event.getCode() == KeyCode.DELETE) {
                removeSongButton.fire();
                event.consume();
            }
        });

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

    private String formatTime(
            double seconds
    ) {

        int totalSeconds =
                (int) seconds;

        int minutes =
                totalSeconds / 60;

        int remainingSeconds =
                totalSeconds % 60;

        return String.format(
                "%02d:%02d",
                minutes,
                remainingSeconds
        );
    }

    public static void main(String[] args) {

        launch(args);
    }
}