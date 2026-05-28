package sonus.service;
import sonus.model.SavedPlaybackState;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import sonus.model.Song;

import java.io.FileReader;
import java.io.FileWriter;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;

// =========================
// Playlist Storage Service
// =========================

public class PlaylistStorageService {

    private static final String PLAYLIST_FILE =
            "playlist.json";

    private final Gson gson =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    // =========================
    // Save Playlist
    // =========================

    public void savePlaybackState(

            List<Song> playlist,

            List<Song> queue
    ) {

        try (

                FileWriter writer =
                        new FileWriter(
                                PLAYLIST_FILE
                        )

        ) {

            SavedPlaybackState state =
                    new SavedPlaybackState();

            state.setPlaylist(
                    playlist
            );

            state.setQueue(
                    queue
            );

            gson.toJson(
                    state,
                    writer
            );

            System.out.println(
                    "Playback state saved"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =========================
    // Load Playlist
    // =========================

    public SavedPlaybackState
    loadPlaybackState() {

        try (

                FileReader reader =
                        new FileReader(
                                PLAYLIST_FILE
                        )

        ) {

            SavedPlaybackState state =
                    gson.fromJson(

                            reader,

                            SavedPlaybackState.class
                    );

            if (state == null) {

                return new SavedPlaybackState();
            }

            System.out.println(
                    "Playback state loaded"
            );

            return state;

        } catch (Exception e) {

            return new SavedPlaybackState();
        }
    }
}