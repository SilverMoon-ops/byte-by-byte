package sonus.service;

import sonus.core.PlaylistManager;
import sonus.core.MetadataExtractor;
import sonus.model.Song;

import java.io.File;

public class PlaylistService {

    private final PlaylistManager playlistManager;

    private boolean isSupportedAudioFile(String fileName) {

        String lowerName = fileName.toLowerCase();

        return lowerName.endsWith(".mp3") ||
                lowerName.endsWith(".wav");
    }

    public PlaylistService(
            PlaylistManager playlistManager
    ) {

        this.playlistManager = playlistManager;
    }

    public int addFolder(String folderPath) {

        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {

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

            if (!isSupportedAudioFile(file.getName())) {
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

}