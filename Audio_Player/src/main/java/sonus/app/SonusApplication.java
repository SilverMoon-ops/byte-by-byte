package sonus.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;

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

                // Load song if none loaded
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

                // Play or resume
                engine.play();

                playPauseButton.setText("⏸");

            } catch (Exception e) {

                e.printStackTrace();
            }
        });

        Button previousButton =
                new Button("⏮");

        Button stopButton =
                new Button("⏹");

        Button nextButton =
                new Button("⏭");

        previousButton.setPrefSize(50, 40);

        playPauseButton.setPrefSize(50, 40);

        stopButton.setPrefSize(50, 40);

        nextButton.setPrefSize(50, 40);


        HBox controls =
                new HBox(
                        15,
                        previousButton,
                        playPauseButton,
                        stopButton,
                        nextButton
                );

        controls.setAlignment(Pos.CENTER);

        controls.setPadding(
                new Insets(20)
        );

        Slider progressSlider =
                new Slider();

        Label currentTimeLabel =
                new Label("00:00");

        Label totalTimeLabel =
                new Label("00:00");

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

        VBox bottomSection =
                new VBox();

        bottomSection.getChildren()
                .addAll(
                        progressSection,
                        controls
                );


        BorderPane root =
                new BorderPane();

        root.setBottom(bottomSection);



        Scene scene =
                new Scene(root, 800, 500);

        stage.setTitle("Sonus");

        stage.setScene(scene);

        stage.show();

        stopButton.setOnAction(event -> {

            try {

                engine.stop();

                Song song =
                        new Song(

                                "D:/Program Files/Symphony No.6 (1st movement).m4a",

                                "Symphony No.6",

                                "Unknown Artist",

                                "m4a",

                                244
                        );

                engine.load(song);

                playPauseButton.setText("▶");

            } catch (Exception e) {

                e.printStackTrace();
            }
        });

        nextButton.setOnAction(event -> {

            System.out.println("Next");
        });

        previousButton.setOnAction(event -> {

            System.out.println("Previous");
        });



    }



    public static void main(String[] args) {

        launch(args);
    }
}