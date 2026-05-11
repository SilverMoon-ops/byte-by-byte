package sonus.core;

import sonus.model.Song;
import sonus.exception.InvalidOperationException;

import javax.sound.sampled.*;
import java.io.File;

public class PlayerEngine {

    private PlayerState state = PlayerState.STOPPED;
    private Song currentSong;
    private Clip clip; // 🔥 actual audio object
    private Runnable onSongFinished;
    private boolean manuallyStopped = false;

    // Load a song
    public void load(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("Song cannot be null");
        }

        // Stop previous clip if exists
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }

        this.currentSong = song;
        this.state = PlayerState.STOPPED;

        System.out.println("Song loaded: " + song);
    }

    // Play song
    public void play() {
        if (currentSong == null) {
            throw new InvalidOperationException("No song loaded");
        }

        try {
            // Load clip if first time
            if (clip == null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                        new File(currentSong.getFilePath())
                );

                clip = AudioSystem.getClip();
                clip.open(audioStream);

                clip.addLineListener(event -> {

                    if (event.getType() == LineEvent.Type.STOP) {

                        // Natural song completion
                        if (!manuallyStopped &&
                                clip.getMicrosecondPosition() >= clip.getMicrosecondLength()) {

                            state = PlayerState.STOPPED;

                            System.out.println("Song finished");

                            if (onSongFinished != null) {
                                onSongFinished.run();
                            }
                        }
                    }
                });
            }

            if (state == PlayerState.PLAYING) {
                throw new InvalidOperationException("Song is already playing");
            }

            if (state == PlayerState.PAUSED) {
                manuallyStopped = false;
                clip.start(); // resume
                state = PlayerState.PLAYING;
                System.out.println("Resuming: " + currentSong);
                return;
            }

            // Start from beginning
            clip.setFramePosition(0);
            manuallyStopped = false;
            clip.start();

            state = PlayerState.PLAYING;
            System.out.println("Playing: " + currentSong);

        } catch (Exception e) {
            throw new RuntimeException("Error playing audio: " + e.getMessage());
        }
    }

    // Pause song
    public void pause() {
        if (state == PlayerState.STOPPED) {
            throw new InvalidOperationException("Cannot pause. Player is stopped.");
        }

        if (state == PlayerState.PAUSED) {
            throw new InvalidOperationException("Song is already paused");
        }
        manuallyStopped = true;
        clip.stop(); // 🔥 actual pause
        state = PlayerState.PAUSED;

        System.out.println("Paused: " + currentSong);
        return;
    }

    // Stop song
    public void stop() {
        if (state == PlayerState.STOPPED) {
            throw new InvalidOperationException("Player is already stopped");
        }

        manuallyStopped = true;
        clip.stop();
        clip.setFramePosition(0); // reset to start

        state = PlayerState.STOPPED;

        System.out.println("Stopped: " + currentSong);
    }

    // Get current state
    public PlayerState getState() {
        return state;
    }

    // Get current song
    public Song getCurrentSong() {
        return currentSong;
    }
    public void setOnSongFinished(Runnable onSongFinished){
        this.onSongFinished = onSongFinished;
    }
}