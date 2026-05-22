package sonus.app;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import sonus.command.CommandHandler;
import sonus.core.FFmpegPlayerEngine;
import sonus.service.PlaylistService;
import sonus.core.PlaylistManager;
import sonus.model.Song;
import sonus.core.AudioEngine;

import java.util.Scanner;

public class MainApp {

    // ✅ Static fields — accessible inside lambda and main()
    private static boolean repeatPlaylist = false;
    private static boolean repeatSingle = false;

    public static void main(String[] args) {

        new JFXPanel();

        Scanner scanner = new Scanner(System.in);

        AudioEngine engine = new FFmpegPlayerEngine();

        PlaylistManager playlistManager = new PlaylistManager();

        PlaylistService playlistService =
                new PlaylistService(playlistManager);

        CommandHandler commandHandler =
                new CommandHandler(
                        engine,
                        playlistManager,
                        playlistService
                );

        // ✅ Updated onSongFinished with repeat logic
        engine.setOnSongFinished(() -> {

            try {

                Song nextSong;

                if (repeatSingle) {

                    nextSong = engine.getCurrentSong();

                } else {

                    nextSong = playlistManager.nextSong();

                    if (nextSong == null && repeatPlaylist) {

                        playlistManager.reset();

                        nextSong = playlistManager.nextSong();
                    }
                }

                if (nextSong != null) {

                    engine.load(nextSong);

                    engine.play();

                    System.out.println(
                            "\n[Auto-play] Now playing: "
                                    + nextSong.getTitle()
                    );

                    System.out.print("> ");

                } else {

                    System.out.println("\nReached end of playlist");

                    System.out.print("> ");
                }

            } catch (Exception e) {

                System.out.println("\nPlayback error");

                e.printStackTrace();
            }
        });

        System.out.println("-------- Welcome to Sonus 🎧 --------");
        System.out.println("Type 'help' to see commands");

        while (true) {

            System.out.print("> ");

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            if (input.equalsIgnoreCase("exit")) {

                try {
                    if (engine.getState() != null) engine.stop();
                } catch (Exception ignored) {}

                Platform.exit();
                scanner.close();
                System.out.println("Goodbye from Sonus 👋");
                break;
            }

            // ✅ Repeat playlist toggle
            if (input.equalsIgnoreCase("repeat")) {

                repeatPlaylist = !repeatPlaylist;

                System.out.println(
                        "Repeat playlist: "
                                + (repeatPlaylist ? "ON" : "OFF")
                );

                continue;
            }

            // ✅ Repeat single toggle
            if (input.equalsIgnoreCase("repeat1")) {

                repeatSingle = !repeatSingle;

                System.out.println(
                        "Repeat single: "
                                + (repeatSingle ? "ON" : "OFF")
                );

                continue;
            }

            boolean handled = commandHandler.handle(input);

            if (!handled) {
                System.out.println("Unknown command. Type 'help'");
            }
        }
    }
}