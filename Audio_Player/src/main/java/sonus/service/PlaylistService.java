package sonus.service;

import sonus.core.MetadataExtractor;
import sonus.core.PlaylistManager;
import sonus.model.Song;

import java.io.File;
import java.util.Set;


public class PlaylistService {



    private final PlaylistManager playlistManager;

    public PlaylistService(
            PlaylistManager playlistManager
    ) {

        this.playlistManager = playlistManager;
    }



    // ADD ENTIRE FOLDER
    public int addFolder(String folderPath) {

        File folder = new File(folderPath);

        if (!folder.exists() ||
                !folder.isDirectory()) {

            throw new IllegalArgumentException(
                    "Invalid folder path"
            );
        }

        File[] files = folder.listFiles();

        if (files == null) {

            return 0;
        }

        int addedCount = 0;

        for (File file : files) {

            if (!file.isFile()) {
                continue;
            }



            try {

                Song song =
                        MetadataExtractor.extract(file);

                playlistManager.addSong(song);

                addedCount++;

                System.out.println(
                        "Added: " + song
                );

            } catch (Exception e) {

                System.out.println(
                        "Failed to load: " +
                                file.getName()
                );
            }
        }

        return addedCount;
    }

    // ADD SINGLE SONG
    public void addSong(String filePath) {

        File file = new File(filePath);

        if (!file.exists() ||
                !file.isFile()) {

            throw new IllegalArgumentException(
                    "Invalid file path"
            );
        }


        try {

            Song song =
                    MetadataExtractor.extract(file);

            playlistManager.addSong(song);

            System.out.println(
                    "Added: " + song
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load song"
            );
        }
    }
}