package sonus.core;

import sonus.model.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {

    // Store Songs
    private final List<Song> playlist;

    // Track Current Song position
    private int currentIndex;

    public PlaylistManager(){
        this.playlist = new ArrayList<>();
        this.currentIndex = -1; // no song selected initially
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
        System.out.println("Added to playlist: " +song);
    }

    // get current song
    public Song getCurrentSong(){

        if(playlist.isEmpty() || currentIndex == -1){
            return null;
        }
        return playlist.get(currentIndex);
    }

    // Move to next song
    public Song nextSong(){

        if(!hasNext()){
            throw new IllegalStateException("No next song available");
        }

        currentIndex++;
        return playlist.get(currentIndex);
    }

    // Move to previous song
    public Song previousSong(){

        if(!hasPrevious()){
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
                System.out.println("->" + song);
            }
            else {
                System.out.println(" " + song);
            }
        }
    }

}