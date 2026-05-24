package sonus.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import sonus.core.AudioEngine;
import sonus.core.FFmpegPlayerEngine;
import sonus.core.PlayerState;
import sonus.model.Song;

public class SonusApplication
        extends Application {

    @Override
    public void start(Stage stage) {

        AudioEngine engine =
                new FFmpegPlayerEngine();

        Button playPauseButton =
                new Button("▶");

        playPauseButton.setOnAction(event -> {

            try {

                // Pause if already playing
                if (
                        engine.getState()
                                == PlayerState.PLAYING
                ) {

                    engine.pause();

                    playPauseButton.setText("▶");

                    return;
                }

                // Load song only once
                if (
                        engine.getCurrentSong()
                                == null
                ) {

                    Song song =
                            new Song(

                                    "D:/Program Files/Symphony No.6 (1st movement).m4a",

                                    "Symphony No.6",

                                    "Unknown Artist",

                                    "m4a",

                                    244
                            );

                    engine.load(song);
                }

                // Resume or play
                engine.play();

                playPauseButton.setText("⏸");

            } catch (Exception e) {

                e.printStackTrace();
            }
        });

        BorderPane root =
                new BorderPane();

        root.setCenter(playPauseButton);

        Scene scene =
                new Scene(root, 800, 500);

        stage.setTitle("Sonus");

        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }
}