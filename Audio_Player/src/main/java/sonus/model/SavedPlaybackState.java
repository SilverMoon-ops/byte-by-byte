package sonus.model;

import java.util.ArrayList;
import java.util.List;

// =========================
// Saved Playback State
// =========================

public class SavedPlaybackState {

    private List<Song> playlist =
            new ArrayList<>();

    private List<Song> queue =
            new ArrayList<>();

    // =========================
    // Playlist
    // =========================

    public List<Song> getPlaylist() {

        return playlist;
    }

    public void setPlaylist(
            List<Song> playlist
    ) {

        this.playlist = playlist;
    }

    // =========================
    // Queue
    // =========================

    public List<Song> getQueue() {

        return queue;
    }

    public void setQueue(
            List<Song> queue
    ) {

        this.queue = queue;
    }
}