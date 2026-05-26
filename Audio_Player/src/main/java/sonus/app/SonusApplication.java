package sonus.app;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;

import sonus.core.AudioEngine;
import sonus.core.FFmpegPlayerEngine;
import sonus.core.PlayerState;
import sonus.core.PlaylistManager;

import sonus.model.Song;
import java.io.File;
import java.util.List;

public class SonusApplication
        extends Application {

    private boolean seeking = false;

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
       // Playlist View Data
      // =========================

        ObservableList<Song> playlistItems =
                FXCollections.observableArrayList(
                        playlistManager.getSongs()
                );

        // =========================
       // Playlist View
      // =========================

        ListView<Song> playlistView =
                new ListView<>(playlistItems);

        playlistView.setPrefWidth(300);

        // =========================
       // Current Song Display
      // =========================

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

                    playlistManager.setCurrentSong(
                            selectedSong
                    );

                    songTitleLabel.setText(
                            selectedSong.getTitle()
                    );

                    artistLabel.setText(
                            selectedSong.getArtist()
                    );

                    engine.play();

                    playPauseButton.setText("⏸");

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
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
                        new Song(

                                selectedFile
                                        .getAbsolutePath(),

                                selectedFile
                                        .getName(),

                                "Unknown Artist",

                                "unknown",

                                0
                        );

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
                            new Song(

                                    file.getAbsolutePath(),

                                    file.getName(),

                                    "Unknown Artist",

                                    "unknown",

                                    0
                            );

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
                            new Song(

                                    file.getAbsolutePath(),

                                    file.getName(),

                                    "Unknown Artist",

                                    "unknown",

                                    0
                            );

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

        Button previousButton =
                new Button("⏮");

        playPauseButton =
                new Button("▶");

        Button stopButton =
                new Button("⏹");

        Button nextButton =
                new Button("⏭");

        previousButton.setPrefSize(50, 40);

        playPauseButton.setPrefSize(50, 40);

        stopButton.setPrefSize(50, 40);

        nextButton.setPrefSize(50, 40);

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

                // Load current playlist song
                if (
                        engine.getCurrentSong()
                                == null
                ) {

                    Song currentSong =
                            playlistManager.getCurrentSong();

                    if (currentSong == null) {
                        return;
                    }

                    engine.load(currentSong);

                    songTitleLabel.setText(
                            currentSong.getTitle()
                    );

                    artistLabel.setText(
                            currentSong.getArtist()
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

                Song currentSong =
                        playlistManager.getCurrentSong();

                if (currentSong != null) {

                    engine.load(currentSong);
                }

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

                engine.stop();

                engine.load(nextSong);

                engine.play();

                songTitleLabel.setText(
                        nextSong.getTitle()
                );

                artistLabel.setText(
                        nextSong.getArtist()
                );

                playPauseButton.setText("⏸");

                playlistView
                        .getSelectionModel()
                        .select(nextSong);

            } catch (Exception e) {

                System.out.println(
                        "No next song"
                );
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

                engine.play();

                songTitleLabel.setText(
                        previousSong.getTitle()
                );

                artistLabel.setText(
                        previousSong.getArtist()
                );

                playPauseButton.setText("⏸");

                playlistView
                        .getSelectionModel()
                        .select(previousSong);

            } catch (Exception e) {

                System.out.println(
                        "No previous song"
                );
            }
        });

        // =========================
        // Controls Layout
        // =========================

        HBox controls =
                new HBox(
                        15,
                        previousButton,
                        playPauseButton,
                        stopButton,
                        nextButton
                );

        controls.setAlignment(
                Pos.CENTER
        );

        // =========================
        // Progress Layout
        // =========================

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

        // =========================
        // Bottom Section
        // =========================

        VBox bottomSection =
                new VBox(
                        12,
                        progressSection,
                        controls,
                        volumeSection
                );

        bottomSection.setAlignment(
                Pos.CENTER
        );

        bottomSection.setPadding(
                new Insets(15)
        );

        // =========================
       // Toolbar
      // =========================

        ToolBar toolBar =
                new ToolBar(

                        addSongButton,

                        addMultipleButton,

                        addFolderButton
                );

        // =========================
       // Top Section
      // =========================

        VBox topSection =
                new VBox(
                        5,
                        nowPlayingLabel,
                        songTitleLabel,
                        artistLabel
                );

        topSection.setAlignment(
                Pos.CENTER
        );

        topSection.setPadding(
                new Insets(20)
        );

        // =========================
        // Root Layout
        // =========================

        BorderPane root =
                new BorderPane();

        VBox topContainer =
                new VBox(
                        toolBar,
                        topSection
                );

        root.setTop(topContainer);
        root.setCenter(playlistView);
        root.setBottom(bottomSection);

        // =========================
        // Scene
        // =========================

        Scene scene =
                new Scene(
                        root,
                        900,
                        600
                );

        stage.setTitle("Sonus");

        stage.setScene(scene);

        stage.show();
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