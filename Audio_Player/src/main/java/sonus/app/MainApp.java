package sonus.app;

import sonus.core.PlayerEngine;
import sonus.core.PlaylistManager;
import sonus.model.Song;

import java.io.File;
import java.util.Scanner;

public class MainApp {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        PlayerEngine engine = new PlayerEngine();
        PlaylistManager playlistManager = new PlaylistManager();

        System.out.println("Welcome to Sonus 🎧");

        while (true) {

            System.out.print("> ");
            String input = scanner.nextLine().trim();

            try {

                // EXIT
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                // ADD SONG
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
                            filePath,
                            file.getName(),
                            "Unknown",
                            "wav",
                            0
                    );

                    playlistManager.addSong(song);
                }

                // PLAY CURRENT SONG
                else if (input.equalsIgnoreCase("play")) {

                    Song currentSong = playlistManager.getCurrentSong();

                    if (currentSong == null) {
                        System.out.println("Playlist is empty");
                        continue;
                    }

                    engine.load(currentSong);
                    engine.play();
                }

                // NEXT SONG
                else if (input.equalsIgnoreCase("next")) {

                    Song nextSong = playlistManager.nextSong();

                    engine.load(nextSong);
                    engine.play();
                }

                // PREVIOUS SONG
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
}