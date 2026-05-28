package sonus.app;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

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
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TextField;

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
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;

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

        // =========================
       // Playlist View
      // =========================

        ListView<Song> playlistView =
                new ListView<>(filteredSongs);

        playlistView.setPrefWidth(700);

        playlistView.setMaxWidth(
                Double.MAX_VALUE
        );

        ListView<Song> queueView =
                new ListView<>(queueItems);

        queueView.setPrefWidth(700);

        queueView.setPrefHeight(500);

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

        MenuItem queueSongItem =
                new MenuItem(
                        "Add To Queue"
                );

        playlistContextMenu.getItems().add(
                queueSongItem
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


       // Current Song Display



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

                        // =========================
                        // Empty Search
                        // =========================

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

                        // =========================
                        // Match Title
                        // =========================

                        if (
                                song.getTitle()
                                        .toLowerCase()
                                        .contains(search)
                        ) {

                            return true;
                        }

                        // =========================
                        // Match Artist
                        // =========================

                        return song.getArtist()
                                .toLowerCase()
                                .contains(search);
                    });
                }
        );

        Label nowPlayingLabel =
                new Label("Now Playing");

        Label songTitleLabel =
                new Label("No song selected");

        Label artistLabel =
                new Label("");

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

                            artistLabel
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

                            artistLabel
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

        volumeSlider =
                new Slider(
                        0,
                        100,
                        100
                );

        volumeSlider.setPrefWidth(180);

        volumeSlider.valueProperty().addListener(

                (
                        observable,
                        oldValue,
                        newValue
                ) -> {

                    engine.setVolume(
                            newValue.doubleValue()
                    );
                }
        );

        HBox volumeSection =
                new HBox(
                        10,
                        new Label("🔊"),
                        volumeSlider
                );

        volumeSection.setAlignment(
                Pos.CENTER
        );

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



                // =========================
               // Auto Select First Song
              // =========================

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

                            artistLabel
                    );
                    try {

                        engine.load(firstSong);

                        engine.play();

                        playPauseButton.setText("⏸");

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
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

                // Pause
                if (
                        engine.getState()
                                == PlayerState.PLAYING
                ) {

                    engine.pause();

                    playPauseButton.setText("▶");

                    return;
                }

                // Load song when stopped
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

                            artistLabel
                    );
                }

                // Play / Resume
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
                // Don't reload until user explicitly plays
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

                        artistLabel
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

                        artistLabel
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

            // =========================
            // OFF → Repeat Playlist
            // =========================

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

            // =========================
            // Repeat Playlist → Repeat One
            // =========================

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

            // =========================
            // Repeat One → OFF
            // =========================

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

            // Stop if removing currently playing song
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


        // Queue Selected Song

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


       // Add Song To Queue


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
        });


        // =====================================================================
        // 1. Sleek Left Sidebar (Reduced Width Rail)
        // =====================================================================

        VBox sidebar =
                new VBox(

                        12
                );

        // Adjusted width down by ~20%
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

        // Constrain buttons to safely fill the new narrower rail width
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
        // 2. Now Playing Display (With New Album Artwork Canvas Architecture)
        // =====================================================================

        nowPlayingLabel.setText(
                "NOW PLAYING"
        );

        // Layout canvas placeholder for cover art
        ImageView albumArtView =
                new ImageView();

        albumArtView.setFitWidth(
                65
        );

        albumArtView.setFitHeight(
                65
        );

        VBox metadataTextSection =
                new VBox(

                        4,

                        nowPlayingLabel,

                        songTitleLabel,

                        artistLabel
                );

        HBox nowPlayingHeader =
                new HBox(

                        15,

                        albumArtView,

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

        VBox playlistSection =
                new VBox(

                        12,

                        nowPlayingHeader,

                        searchField,

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

        // Compacted queue utility controls
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

        // Allow queue to dynamically grow vertically without forcing stiff heights
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

        // Eliminated topContainer layout block entirely to prevent upper row crowding
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
        // Scene
        // =========================

        Scene scene =
                new Scene(
                        root,
                        900,
                        600
                );

        // =========================
       // Keyboard Shortcuts
      // =========================

        scene.addEventHandler(
                KeyEvent.KEY_PRESSED,

                event -> {

                    // =========================
                    // Play / Pause
                    // =========================

                    if (
                            event.getCode()
                                    == KeyCode.SPACE
                    ) {

                        playPauseButton.fire();
                    }

                    // =========================
                    // Next Song
                    // =========================

                    else if (
                            event.getCode()
                                    == KeyCode.RIGHT
                    ) {

                        nextButton.fire();
                    }

                    // =========================
                    // Previous Song
                    // =========================

                    else if (
                            event.getCode()
                                    == KeyCode.LEFT
                    ) {

                        previousButton.fire();
                    }

                    // =========================
                    // Remove Song
                    // =========================

                    else if (
                            event.getCode()
                                    == KeyCode.DELETE
                    ) {

                        removeSongButton.fire();
                    }
                }
        );

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

                    // =========================
                    // Folder Import
                    // =========================

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

                    // =========================
                    // Single File Import
                    // =========================

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

                // =========================
                // Auto Select First Song
                // =========================

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

                            artistLabel
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

    // =========================
// Sync Current Song UI
// =========================

    private void updateCurrentSongUI(

            Song song,

            ListView<Song> playlistView,

            Label songTitleLabel,

            Label artistLabel
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

            // =========================
            // Fallback Title
            // =========================

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

            // =========================
            // Fallback Artist
            // =========================

            if (
                    artist == null
                            ||
                            artist.isBlank()
            ) {

                artist =
                        "Unknown Artist";
            }

            // =========================
            // File Extension
            // =========================

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