package sonus.core;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import sonus.model.Song;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.audio.AudioHeader;

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

    public static Song extract(File file) {

        try {

            AudioFile audioFile =
                    AudioFileIO.read(file);

            Tag tag = audioFile.getTag();

            AudioHeader header =
                    audioFile.getAudioHeader();

            String title = file.getName();

            String artist = "Unknown Artist";

            if (tag != null) {

                String extractedTitle =
                        tag.getFirst(FieldKey.TITLE);

                String extractedArtist =
                        tag.getFirst(FieldKey.ARTIST);

                if (extractedTitle != null &&
                        !extractedTitle.isBlank()) {

                    title = extractedTitle;
                }

                if (extractedArtist != null &&
                        !extractedArtist.isBlank()) {

                    artist = extractedArtist;
                }
            }

            String format =
                    header.getFormat();

            long duration =
                    header.getTrackLength() * 1000L;

            return new Song(
                    file.getAbsolutePath(),
                    title,
                    artist,
                    format,
                    duration
            );

        } catch (Exception e) {

            return new Song(
                    file.getAbsolutePath(),
                    file.getName(),
                    "Unknown Artist",
                    "unknown",
                    0
            );
        }
    }

}