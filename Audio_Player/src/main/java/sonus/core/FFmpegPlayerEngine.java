package sonus.core;

import sonus.exception.InvalidOperationException;
import sonus.model.Song;

import java.io.IOException;

public class FFmpegPlayerEngine
        implements AudioEngine {

    private Process playbackProcess;

    private Song currentSong;

    private PlayerState state =
            PlayerState.STOPPED;

    @Override
    public void load(Song song) {

        if (song == null) {

            throw new IllegalArgumentException(
                    "Song cannot be null"
            );
        }

        this.currentSong = song;
    }

    @Override
    public void play() {

        if (currentSong == null) {

            throw new InvalidOperationException(
                    "No song loaded"
            );
        }

        if (playbackProcess != null &&
                playbackProcess.isAlive()) {

            stop();
        }

        try {

            stop();

            playbackProcess =
                    new ProcessBuilder(
                            "ffplay",
                            "-nodisp",
                            "-autoexit",
                            "-loglevel",
                            "quiet",
                            currentSong.getFilePath()
                    ).start();

            state = PlayerState.PLAYING;

            System.out.println(
                    "Playing: " + currentSong
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to start ffplay"
            );
        }
    }

    @Override
    public void stop() {

        if (playbackProcess != null &&
                playbackProcess.isAlive()) {

            playbackProcess.destroyForcibly();

            try {

                playbackProcess.waitFor();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }

            playbackProcess = null;
        }

        state = PlayerState.STOPPED;

        System.out.println(
                "Playback stopped"
        );
    }

    @Override
    public void pause() {

        System.out.println(
                "Pause not implemented yet"
        );
    }

    @Override
    public void seek(double seconds) {

        System.out.println(
                "Seek not implemented yet"
        );
    }

    @Override
    public void setVolume(double volume) {

        System.out.println(
                "Volume control not implemented yet"
        );
    }

    @Override
    public int getVolume() {

        return 100;
    }

    @Override
    public double getCurrentTime() {

        return 0;
    }

    @Override
    public double getTotalDuration() {

        return 0;
    }

    @Override
    public Song getCurrentSong() {

        return currentSong;
    }

    @Override
    public PlayerState getState() {

        return state;
    }

    @Override
    public void setOnSongFinished(
            Runnable callback
    ) {

    }

    @Override
    public void setOnProgressUpdate(
            Runnable callback
    ) {

    }
}