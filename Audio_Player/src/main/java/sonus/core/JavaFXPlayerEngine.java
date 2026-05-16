package sonus.core;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import sonus.core.AudioEngine;

import sonus.exception.InvalidOperationException;
import sonus.model.Song;

import java.io.File;

public class JavaFXPlayerEngine implements AudioEngine {

    private Timeline progressTimeline;

    private Runnable onProgressUpdate;

    private PlayerState state = PlayerState.STOPPED;

    private Song currentSong;

    private MediaPlayer mediaPlayer;

    private Runnable onSongFinished;

    private double volume = 100;

    private void startProgressUpdates() {

        stopProgressUpdates();

        progressTimeline = new Timeline(
                new KeyFrame(
                        Duration.millis(500),
                        event -> {

                            if (onProgressUpdate != null) {

                                onProgressUpdate.run();
                            }
                        }
                )
        );

        progressTimeline.setCycleCount(
                Timeline.INDEFINITE
        );

        progressTimeline.play();
    }

    private void stopProgressUpdates() {

        if (progressTimeline != null) {

            progressTimeline.stop();
        }
    }


    public void setOnProgressUpdate(
            Runnable onProgressUpdate
    ) {

        this.onProgressUpdate =
                onProgressUpdate;
    }


    // LOAD SONG
    public void load(Song song) {

        if (song == null) {
            throw new IllegalArgumentException("Song cannot be null");
        }

        // Dispose previous player
        if (mediaPlayer != null) {

            mediaPlayer.stop();

            mediaPlayer.dispose();
        }

        this.currentSong = song;

        this.state = PlayerState.STOPPED;

        try {

            File file = new File(song.getFilePath());

            Media media = new Media(file.toURI().toString());

            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setVolume(volume / 100.0);

            // Song completion callback
            mediaPlayer.setOnEndOfMedia(() -> {

                state = PlayerState.STOPPED;

                System.out.println("Song finished");

                if (onSongFinished != null) {

                    onSongFinished.run();
                }
            });

            System.out.println("Song loaded: " + song);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load media: " + e.getMessage()
            );
        }
    }

    // PLAY
    public void play() {

        if (mediaPlayer == null ||
                currentSong == null) {

            throw new InvalidOperationException(
                    "No song loaded"
            );
        }

        if (state == PlayerState.PLAYING) {

            System.out.println(
                    "Song is already playing"
            );

            return;
        }

        mediaPlayer.play();

        startProgressUpdates();

        state = PlayerState.PLAYING;

        System.out.println(
                "Playing: " + currentSong
        );
    }

    // PAUSE
    public void pause() {

        if (mediaPlayer == null ||
                currentSong == null) {

            System.out.println(
                    "No song loaded"
            );

            return;
        }

        if (state != PlayerState.PLAYING) {

            System.out.println(
                    "No song is playing"
            );

            return;
        }

        mediaPlayer.pause();

        stopProgressUpdates();

        state = PlayerState.PAUSED;

        System.out.println(
                "Paused: " + currentSong
        );
    }

    // STOP
    public void stop() {

        if (mediaPlayer == null ||
                currentSong == null) {

            System.out.println(
                    "No song loaded"
            );

            return;
        }

        if (state == PlayerState.STOPPED) {

            System.out.println(
                    "Player is already stopped"
            );

            return;
        }

        mediaPlayer.stop();

        stopProgressUpdates();

        state = PlayerState.STOPPED;

        System.out.println(
                "Stopped: " + currentSong
        );
    }

    // GET STATE
    public PlayerState getState() {

        return state;
    }

    // GET CURRENT SONG
    public Song getCurrentSong() {

        return currentSong;
    }

    // CALLBACK
    public void setOnSongFinished(Runnable onSongFinished) {

        this.onSongFinished = onSongFinished;
    }

    public double getCurrentTime() {

        if (mediaPlayer == null) {
            return 0;
        }

        return mediaPlayer.getCurrentTime().toSeconds();
    }

    public double getTotalDuration() {

        if (mediaPlayer == null ||
                mediaPlayer.getTotalDuration() == null) {

            return 0;
        }

        return mediaPlayer.getTotalDuration().toSeconds();
    }

    public void seek(double seconds) {

        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.seek(javafx.util.Duration.seconds(seconds));
    }

    public void setVolume(double volume) {

        // Clamp between 0 and 100
        volume = Math.max(0, Math.min(volume, 100));

        this.volume = volume;

        if (mediaPlayer != null) {

            mediaPlayer.setVolume(volume / 100.0);
        }
    }

    public int getVolume() {

        return (int) volume;
    }
}