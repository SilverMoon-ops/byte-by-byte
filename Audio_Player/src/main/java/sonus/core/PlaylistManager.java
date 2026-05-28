package sonus.core;

import sonus.model.Song;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Queue;

public class PlaylistManager {

    private final List<Song> playlist;
    private boolean repeatSingleSong;
    private boolean shuffleEnabled;
    private List<Integer> shuffleOrder;
    private int shuffleIndex;
    private boolean repeatPlaylist;
    private int currentIndex;

    // Track historical indices safely
    private final Stack<Integer> playbackHistory = new Stack<>();
    private final Queue<Song>
            playbackQueue = new LinkedList<>();

    private final Object lock = new Object();

    public PlaylistManager(){
        this.playlist = new ArrayList<>();
        this.currentIndex = -1;
        this.repeatPlaylist = false;
        this.repeatSingleSong = false;
        this.shuffleEnabled = false;
        this.shuffleOrder = new ArrayList<>();
    }

    public boolean containsSong(String filePath) {
        synchronized (lock) {
            for (Song song : playlist) {
                if (song.getFilePath().equalsIgnoreCase(filePath)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void addSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("Song cannot be null");
        }
        synchronized (lock) {
            if (containsSong(song.getFilePath())) {
                System.out.println("Song already exists in playlist");
                return;
            }
            playlist.add(song);
            System.out.println("Added to playlist: " + song);

            if (currentIndex == -1) {
                currentIndex = 0;
            }
        }
    }

    public List<Song> getSongs() {
        synchronized (lock) {
            return new ArrayList<>(playlist);
        }
    }

    public void removeSong(int index) {
        synchronized (lock) {
            if (index < 0 || index >= playlist.size()) {
                throw new IllegalArgumentException("Invalid song index");
            }

            Song removedSong = playlist.remove(index);

            // Adjust current index dynamically
            if (playlist.isEmpty()) {
                currentIndex = -1;
                playbackHistory.clear();
            } else {
                if (index < currentIndex) {
                    currentIndex--;
                } else if (index == currentIndex) {
                    if (currentIndex >= playlist.size()) {
                        currentIndex = playlist.size() - 1;
                    }
                }
                // Wipe history entries pointing to dead indices safely
                playbackHistory.removeElement(index);
            }

            System.out.println("Removed: " + removedSong);

            if (shuffleEnabled) {
                generateShuffleOrder();
                shuffleIndex = 0;
            }
        }
    }

    // FIX 1: Added back the Object-based removeSong method your UI calls!
    public void removeSong(Song song) {
        synchronized (lock) {
            int index = playlist.indexOf(song);
            if (index != -1) {
                removeSong(index); // Routes directly into our safe index remover logic
            }
        }
    }

    public void clearPlaylist() {
        synchronized (lock) {
            playlist.clear();
            shuffleOrder.clear();
            playbackHistory.clear();
            currentIndex = -1;
        }
    }

    // FIX 2: Added back the clear() alias method your UI calls!
    public void clear() {
        clearPlaylist(); // Routes directly into our main clear logic
    }

    public Song getCurrentSong(){
        synchronized (lock) {
            if(playlist.isEmpty() || currentIndex == -1){
                return null;
            }
            return playlist.get(currentIndex);
        }
    }

    public void setCurrentSong(Song song) {
        synchronized (lock) {
            int index = playlist.indexOf(song);
            if (index != -1) {
                if (currentIndex >= 0) {
                    playbackHistory.push(currentIndex); // Track explicit selections
                }
                currentIndex = index;
            }
        }
    }

    public void setRepeatPlaylist(boolean repeatPlaylist) {
        synchronized (lock) { this.repeatPlaylist = repeatPlaylist; }
    }

    public boolean isRepeatPlaylist() {
        synchronized (lock) { return repeatPlaylist; }
    }

    public void setRepeatSingleSong(boolean repeatSingleSong) {
        synchronized (lock) { this.repeatSingleSong = repeatSingleSong; }
    }

    public boolean isRepeatSingleSong() {
        synchronized (lock) { return repeatSingleSong; }
    }

    public void enableShuffle() {
        synchronized (lock) {
            if (playlist.isEmpty()) return;
            shuffleEnabled = true;
            generateShuffleOrder();
            shuffleIndex = 0;
            System.out.println("Shuffle enabled");
        }
    }

    public void disableShuffle() {
        synchronized (lock) {
            shuffleEnabled = false;
            shuffleOrder.clear();
            shuffleIndex = 0;
            System.out.println("Shuffle disabled");
        }
    }

    public boolean isShuffleEnabled() {
        synchronized (lock) { return shuffleEnabled; }
    }

    private void generateShuffleOrder() {
        shuffleOrder.clear();
        if (currentIndex >= 0 && currentIndex < playlist.size()) {
            shuffleOrder.add(currentIndex);
        }
        List<Integer> remaining = new ArrayList<>();
        for (int i = 0; i < playlist.size(); i++) {
            if (i != currentIndex) {
                remaining.add(i);
            }
        }
        Collections.shuffle(remaining);
        shuffleOrder.addAll(remaining);
    }

    public Song nextSong() {
        synchronized (lock) {

            if (!playbackQueue.isEmpty()) {

                Song queuedSong =
                        playbackQueue.poll();

                int queuedIndex =
                        playlist.indexOf(
                                queuedSong
                        );

                if (queuedIndex >= 0) {

                    playbackHistory.push(
                            currentIndex
                    );

                    currentIndex =
                            queuedIndex;

                    if (shuffleEnabled) {

                        int sIdx =
                                shuffleOrder.indexOf(
                                        currentIndex
                                );

                        if (sIdx >= 0) {

                            shuffleIndex = sIdx;
                        }
                    }
                }

                return queuedSong;
            }

            if (playlist.isEmpty()) {
                throw new IllegalStateException("Playlist is empty");
            }
            if (repeatSingleSong && currentIndex >= 0) {
                return playlist.get(currentIndex);
            }

            // Always capture current track in history before changing
            if (currentIndex >= 0) {
                playbackHistory.push(currentIndex);
            }

            if (shuffleEnabled) {
                if (shuffleIndex < shuffleOrder.size() - 1) {
                    shuffleIndex++;
                } else if (repeatPlaylist) {
                    generateShuffleOrder();
                    shuffleIndex = 0;
                } else {
                    playbackHistory.pop(); // Revert push since move failed
                    throw new IllegalStateException("No next song available");
                }
                currentIndex = shuffleOrder.get(shuffleIndex);
                return playlist.get(currentIndex);
            }

            // Normal linear tracking logic
            if (hasNext()) {
                currentIndex++;
            } else if (repeatPlaylist) {
                currentIndex = 0;
            } else {
                playbackHistory.pop(); // Revert push since move failed
                throw new IllegalStateException("No next song available");
            }

            return playlist.get(currentIndex);
        }
    }

    public Song previousSong() {
        synchronized (lock) {
            if (playlist.isEmpty()) {
                throw new IllegalStateException("Playlist is empty");
            }

            // True history tracking using our Stack state
            if (!playbackHistory.isEmpty()) {
                currentIndex = playbackHistory.pop();

                // Keep shuffle index in lockstep if shuffle is active
                if (shuffleEnabled) {
                    int sIdx = shuffleOrder.indexOf(currentIndex);
                    if (sIdx != -1) {
                        shuffleIndex = sIdx;
                    }
                }
                return playlist.get(currentIndex);
            }

            // Fallback strategy if stack history is empty
            if (shuffleEnabled) {
                if (shuffleIndex <= 0) throw new IllegalStateException("No previous track history");
                shuffleIndex--;
                currentIndex = shuffleOrder.get(shuffleIndex);
            } else {
                if (!hasPrevious()) throw new IllegalStateException("No previous track history");
                currentIndex--;
            }

            return playlist.get(currentIndex);
        }
    }

    public boolean hasNext(){
        synchronized (lock) { return currentIndex < playlist.size() - 1; }
    }

    public boolean hasPrevious(){
        synchronized (lock) {
            // Better check: Can go back if history exists OR index is greater than 0
            return !playbackHistory.isEmpty() || currentIndex > 0;
        }
    }

    public int size(){ synchronized (lock) { return playlist.size(); } }

    public boolean isEmpty(){ synchronized (lock) { return playlist.isEmpty(); } }

    public void showPlaylist(){
        synchronized (lock) {
            if (playlist.isEmpty()){
                System.out.println("Playlist is empty");
                return;
            }
            System.out.println("----- Playlist -----");
            for (int i = 0; i < playlist.size(); i++){
                Song song  = playlist.get(i);
                if (i == currentIndex){
                    System.out.println("-> [" + i + "] " + song);
                } else {
                    System.out.println("   [" + i + "] " + song);
                }
            }
        }
    }

   // Queue Song


    public void addToQueue(
            Song song
    ) {

        synchronized (lock) {

            if (song == null) {
                return;
            }

            playbackQueue.offer(song);
        }
    }


    // Remove From Queue


    public void removeFromQueue(
            Song song
    ) {

        synchronized (lock) {

            playbackQueue.remove(song);
        }
    }


     // Clear Queue


    public void clearQueue() {

        synchronized (lock) {

            playbackQueue.clear();
        }
    }


      // Get Queue


    public List<Song> getQueue() {

        synchronized (lock) {

            return new ArrayList<>(
                    playbackQueue
            );
        }
    }


     // Has Queue


    public boolean hasQueuedSongs() {

        synchronized (lock) {

            return !playbackQueue.isEmpty();
        }
    }

    public void reset() {
        synchronized (lock) {
            currentIndex = -1;
            playbackHistory.clear();
        }
    }
}