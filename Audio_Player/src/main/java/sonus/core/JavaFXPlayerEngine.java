package sonus.core;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import sonus.exception.InvalidOperationException;
import sonus.model.Song;

import java.io.File;

public class JavaFXPlayerEngine {

    private PlayerState state = PlayerState.STOPPED;

    private Song currentSong;

    private MediaPlayer mediaPlayer;

    private Runnable onSongFinished;

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

        if (currentSong == null) {

            throw new InvalidOperationException("No song loaded");
        }

        if (state == PlayerState.PLAYING) {

            throw new InvalidOperationException(
                    "Song is already playing"
            );
        }

        mediaPlayer.play();

        state = PlayerState.PLAYING;

        System.out.println("Playing: " + currentSong);
    }

    // PAUSE
    public void pause() {

        if (state != PlayerState.PLAYING) {

            throw new InvalidOperationException(
                    "No song is playing"
            );
        }

        mediaPlayer.pause();

        state = PlayerState.PAUSED;

        System.out.println("Paused: " + currentSong);
    }

    // STOP
    public void stop() {

        if (state == PlayerState.STOPPED) {

            throw new InvalidOperationException(
                    "Player is already stopped"
            );
        }

        mediaPlayer.stop();

        state = PlayerState.STOPPED;

        System.out.println("Stopped: " + currentSong);
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
}