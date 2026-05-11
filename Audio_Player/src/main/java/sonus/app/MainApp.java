package sonus.app;

import sonus.core.PlayerEngine;
import sonus.core.PlaylistManager;
import sonus.model.Song;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.Scanner;

public class MainApp {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        PlayerEngine engine = new PlayerEngine();
        PlaylistManager playlistManager = new PlaylistManager();
        engine.setOnSongFinished(() -> {

            try {

                if (playlistManager.hasNext()) {

                    Song nextSong = playlistManager.nextSong();

                    engine.load(nextSong);

                    engine.play();

                } else {

                    System.out.println("End of playlist");
                }

            } catch (Exception e) {

                System.out.println("Auto-play error: " + e.getMessage());
            }
        });

        System.out.println("-------Welcome to Sonus 🎧-------");
        System.out.println("Type 'help' to see commands");

        while (true) {

            System.out.print("> ");
            String input = scanner.nextLine().trim();

            try {

                // HELP
                if (input.equalsIgnoreCase("help")) {

                    System.out.println("""

                            Available Commands:

                            add <filepath>
                            -> Add single WAV file

                            folder <folderpath>
                            -> Load all WAV files from folder

                            play
                            -> Play current song

                            pause
                            -> Pause playback

                            stop
                            -> Stop playback

                            next
                            -> Play next song

                            prev
                            -> Play previous song

                            playlist
                            -> Show playlist

                            exit
                            -> Exit Sonus
                            """);
                }

                // EXIT
                else if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                // LOAD FOLDER
                else if (input.toLowerCase().startsWith("folder")) {

                    String folderPath = input.substring(6).trim();

                    if (folderPath.isEmpty()) {
                        System.out.println("Usage: folder <folderpath>");
                        continue;
                    }

                    // Remove quotes
                    if ((folderPath.startsWith("\"") && folderPath.endsWith("\"")) ||
                            (folderPath.startsWith("'") && folderPath.endsWith("'"))) {

                        folderPath = folderPath.substring(1, folderPath.length() - 1);
                    }

                    File folder = new File(folderPath);

                    if (!folder.exists() || !folder.isDirectory()) {
                        System.out.println("Invalid folder");
                        continue;
                    }

                    File[] files = folder.listFiles();

                    if (files == null || files.length == 0) {
                        System.out.println("Folder is empty");
                        continue;
                    }

                    int addedCount = 0;

                    for (File file : files) {

                        if (file.isFile() &&
                                file.getName().toLowerCase().endsWith(".wav")) {

                            Song song = new Song(
                                    file.getAbsolutePath(),
                                    removeExtension(file.getName()),
                                    "Unknown",
                                    "wav",
                                    getAudioDuration(file)
                            );

                            playlistManager.addSong(song);
                            addedCount++;
                        }
                    }

                    System.out.println("Added " + addedCount + " song(s) to playlist");
                }

                // ADD SINGLE SONG
                else if (input.toLowerCase().startsWith("add")) {

                    String filePath = input.substring(3).trim();

                    if (filePath.isEmpty()) {
                        System.out.println("Usage: add <filepath>");
                        continue;
                    }

                    // Remove quotes
                    if ((filePath.startsWith("\"") && filePath.endsWith("\"")) ||
                            (filePath.startsWith("'") && filePath.endsWith("'"))) {

                        filePath = filePath.substring(1, filePath.length() - 1);
                    }

                    File file = new File(filePath);

                    if (!file.exists()) {
                        System.out.println("File not found");
                        continue;
                    }

                    Song song = new Song(
                            file.getAbsolutePath(),
                            removeExtension(file.getName()),
                            "Unknown",
                            "wav",
                            getAudioDuration(file)
                    );

                    playlistManager.addSong(song);
                }

                // REPEAT PLAYLIST
                else if (input.equalsIgnoreCase("repeat on")) {

                    playlistManager.setRepeatPlaylist(true);

                    System.out.println("Repeat playlist enabled");
                }

                else if (input.equalsIgnoreCase("repeat off")) {

                    playlistManager.setRepeatPlaylist(false);

                    System.out.println("Repeat playlist disabled");
                }

                // PLAY
                else if (input.equalsIgnoreCase("play")) {

                    Song currentSong = playlistManager.getCurrentSong();

                    if (currentSong == null) {
                        System.out.println("Playlist is empty");
                        continue;
                    }

                    engine.load(currentSong);
                    engine.play();
                }

                // NEXT
                else if (input.equalsIgnoreCase("next")) {

                    Song nextSong = playlistManager.nextSong();

                    engine.load(nextSong);
                    engine.play();
                }

                // PREVIOUS
                else if (input.equalsIgnoreCase("prev")) {

                    Song previousSong = playlistManager.previousSong();

                    engine.load(previousSong);
                    engine.play();
                }

                // SHOW PLAYLIST
                else if (input.equalsIgnoreCase("playlist")) {
                    playlistManager.showPlaylist();
                }

                // PAUSE
                else if (input.equalsIgnoreCase("pause")) {
                    engine.pause();
                }

                // STOP
                else if (input.equalsIgnoreCase("stop")) {
                    engine.stop();
                }

                else {
                    System.out.println("Unknown command");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    // Remove file extension
    private static String removeExtension(String filename) {
        return filename.replaceFirst("[.][^.]+$", "");
    }

    // Get WAV duration
    private static long getAudioDuration(File file) {

        try {

            AudioInputStream audioInputStream =
                    AudioSystem.getAudioInputStream(file);

            AudioFormat format = audioInputStream.getFormat();

            long frames = audioInputStream.getFrameLength();

            return (long) ((frames * 1000) / format.getFrameRate());

        } catch (Exception e) {
            return 0;
        }
    }
}