package sonus.core;

import sonus.model.Song;

public interface AudioEngine {

    void load(Song song);

    void play();

    void pause();

    void stop();

    void seek(double seconds);

    void setVolume(double volume);

    int getVolume();

    double getCurrentTime();

    double getTotalDuration();

    Song getCurrentSong();

    PlayerState getState();

    void setOnSongFinished(
            Runnable callback
    );

    void setOnProgressUpdate(
            Runnable callback
    );

    void setSpeed(double speed);
    double getSpeed();
}