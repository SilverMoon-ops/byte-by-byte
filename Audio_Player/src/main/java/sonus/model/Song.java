package sonus.model;

public class Song{
    private final String filePath;
    private final String title;
    private final String artist;
    private final String format;
    private final long duration;

    public Song(String filePath, String title, String artist, String format, long duration) {
        if(filePath == null || filePath.isEmpty()){
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        this.filePath = filePath;
        this.title = (title == null || title.isEmpty()) ? "Unknown Title" : title;
        this.artist = (artist == null || artist.isEmpty()) ? "Unknown Artist" : artist;
        this.format = format;
        this.duration = Math.max(0, duration);
    }


// --- Getters ---
public String getFilePath() { return filePath; }
public String getTitle() { return title; }
public String getArtist() { return artist; }
public String getFormat() { return format; }
public long getDuration() { return duration; }

    public String getFormattedDuration() {

        long totalSeconds = duration / 1000;

        long minutes = totalSeconds / 60;

        long seconds = totalSeconds % 60;

        return String.format(
                "%d:%02d",
                minutes,
                seconds
        );
    }

    @Override
    public String toString() {
        return String.format("%s - %s [%s]", artist, title, getFormattedDuration());
    }
}