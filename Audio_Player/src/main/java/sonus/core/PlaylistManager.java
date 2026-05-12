package sonus.core;

import sonus.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class PlaylistManager {

    // Store Songs
    private final List<Song> playlist;
    private boolean repeatSingleSong;
    private boolean shuffleEnabled;
    private List<Integer> shuffleOrder;
    private int shuffleIndex;

    // Track Current Song position
    private int currentIndex;
    private boolean repeatPlaylist;

    public PlaylistManager(){
        this.playlist = new ArrayList<>();
        this.currentIndex = -1; // no song selected initially
        this.repeatPlaylist = false;
        this.repeatSingleSong = false;
        this.shuffleEnabled = false;
        this.shuffleOrder = new ArrayList<>();
    }

    // add song to playlist
    public void addSong(Song song){
        if(song == null){
            throw new IllegalArgumentException("Song cannot be null");
        }
        playlist.add(song);

        // if first song added, make it current
        if (currentIndex == -1){
            currentIndex = 0;
        }

    }
    public void removeSong(int index) {

        if (index < 0 || index >= playlist.size()) {
            throw new IllegalArgumentException("Invalid song index");
        }

        Song removedSong = playlist.remove(index);

        // Fix current index
        if (playlist.isEmpty()) {

            currentIndex = -1;

        } else if (index < currentIndex) {

            currentIndex--;

        } else if (index == currentIndex) {

            if (currentIndex >= playlist.size()) {
                currentIndex = playlist.size() - 1;
            }
        }

        System.out.println("Removed: " + removedSong);

        // Regenerate shuffle if enabled
        if (shuffleEnabled) {
            generateShuffleOrder();
            shuffleIndex = 0;
        }
    }
    public void clearPlaylist() {

        playlist.clear();

        currentIndex = -1;

        shuffleOrder.clear();

        shuffleIndex = 0;

        System.out.println("Playlist cleared");
    }

    // get current song
    public Song getCurrentSong(){

        if(playlist.isEmpty() || currentIndex == -1){
            return null;
        }
        return playlist.get(currentIndex);
    }

    public void setRepeatPlaylist(boolean repeatPlaylist) {
        this.repeatPlaylist = repeatPlaylist;
    }
    public boolean isRepeatPlaylist() {
        return repeatPlaylist;
    }
    public void setRepeatSingleSong(boolean repeatSingleSong) {
        this.repeatSingleSong = repeatSingleSong;
    }
    public boolean isRepeatSingleSong() {
        return repeatSingleSong;
    }
    public void enableShuffle() {

        if (playlist.isEmpty()) {
            return;
        }

        shuffleEnabled = true;

        generateShuffleOrder();

        // Keep current song position synced
        shuffleIndex = 0;

        System.out.println("Shuffle enabled");
    }

    public void disableShuffle() {

        shuffleEnabled = false;

        shuffleOrder.clear();

        shuffleIndex = 0;

        System.out.println("Shuffle disabled");
    }
    public boolean isShuffleEnabled() {
        return shuffleEnabled;
    }

    private void generateShuffleOrder() {

        shuffleOrder.clear();

        // Current song stays first
        shuffleOrder.add(currentIndex);

        List<Integer> remaining = new ArrayList<>();

        // Add remaining songs
        for (int i = 0; i < playlist.size(); i++) {

            if (i != currentIndex) {
                remaining.add(i);
            }
        }

        // Shuffle remaining songs
        Collections.shuffle(remaining);

        // Combine lists
        shuffleOrder.addAll(remaining);
    }


    // Move to next song
    public Song nextSong() {

        if (playlist.isEmpty()) {
            throw new IllegalStateException("Playlist is empty");
        }
        // Repeat current song
        if (repeatSingleSong) {
            return playlist.get(currentIndex);
        }
        // Shuffle playback
        if (shuffleEnabled) {

            // Move inside shuffle order
            if (shuffleIndex < shuffleOrder.size() - 1) {

                shuffleIndex++;

            }

            // Repeat shuffled playlist
            else if (repeatPlaylist) {

                generateShuffleOrder();

                shuffleIndex = 0;

            }

            // End reached
            else {

                throw new IllegalStateException("No next song available");
            }

            currentIndex = shuffleOrder.get(shuffleIndex);

            return playlist.get(currentIndex);
        }

        // Normal next
        if (hasNext()) {

            currentIndex++;

        }

        // Repeat playlist
        else if (repeatPlaylist) {

            currentIndex = 0;

        }

        // End reached
        else {

            throw new IllegalStateException("No next song available");
        }

        return playlist.get(currentIndex);
    }

    // Move to previous song
    public Song previousSong() {

        // Shuffle previous
        if (shuffleEnabled) {

            if (shuffleIndex <= 0) {
                throw new IllegalStateException("No previous song available");
            }

            shuffleIndex--;

            currentIndex = shuffleOrder.get(shuffleIndex);

            return playlist.get(currentIndex);
        }

        // Normal playback previous
        if (!hasPrevious()) {
            throw new IllegalStateException("No previous song available");
        }

        currentIndex--;

        return playlist.get(currentIndex);
    }


    // check if next song exists
    public boolean hasNext(){
        return  currentIndex < playlist.size() -1;
    }

    // check if previous song exists
    public boolean hasPrevious(){
        return currentIndex > 0;
    }

    // get total playlist size
    public int size(){
        return playlist.size();
    }

    // check if playlist is empty
    public boolean isEmpty(){
        return playlist.isEmpty();
    }

    // print all songs
    public void showPlaylist(){
        if (playlist.isEmpty()){
            System.out.println("playlist is empty");
            return;
        }

        System.out.println("----- Playlist -----");
        for (int i = 0; i < playlist.size(); i++){
            Song song  = playlist.get(i);

            // Highlight current song
            if (i == currentIndex){
                System.out.println("-> [" + i + "] " + song);
            }
            else {
                System.out.println("   [" + i + "] " + song);
            }
        }
    }

}