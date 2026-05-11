package sonus.core;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;

import java.io.File;

public class MetadataExtractor {

    public static String getTitle(File file) {

        try {

            AudioFile audioFile = AudioFileIO.read(file);

            String title = audioFile.getTag().getFirst(FieldKey.TITLE);

            if (title == null || title.isBlank()) {
                return removeExtension(file.getName());
            }

            return title;

        } catch (Exception e) {

            return removeExtension(file.getName());
        }
    }

    public static String getArtist(File file) {

        try {

            AudioFile audioFile = AudioFileIO.read(file);

            String artist = audioFile.getTag().getFirst(FieldKey.ARTIST);

            if (artist == null || artist.isBlank()) {
                return "Unknown";
            }

            return artist;

        } catch (Exception e) {

            return "Unknown";
        }
    }

    public static long getDuration(File file) {

        try {

            AudioFile audioFile = AudioFileIO.read(file);

            return audioFile.getAudioHeader().getTrackLength();

        } catch (Exception e) {

            return 0;
        }
    }

    private static String removeExtension(String fileName) {

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex == -1) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }

}