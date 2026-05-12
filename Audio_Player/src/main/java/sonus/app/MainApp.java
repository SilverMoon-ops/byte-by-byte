package sonus.app;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import sonus.command.CommandHandler;
import sonus.core.JavaFXPlayerEngine;
import sonus.service.PlaylistService;
import sonus.core.PlaylistManager;
import sonus.model.Song;

import java.util.Scanner;

public class MainApp {

    public static void main(String[] args) {

        // Initialize JavaFX Toolkit
        new JFXPanel();

        Scanner scanner = new Scanner(System.in);

        JavaFXPlayerEngine engine = new JavaFXPlayerEngine();

        PlaylistManager playlistManager = new PlaylistManager();

        PlaylistService playlistService =
                new PlaylistService(playlistManager);

        CommandHandler commandHandler =
                new CommandHandler(
                        engine,
                        playlistManager,
                        playlistService
                );

        // Auto play next song
        engine.setOnSongFinished(() -> {

            Platform.runLater(() -> {

                try {

                    Song nextSong =
                            playlistManager.nextSong();

                    if (nextSong != null) {

                        engine.load(nextSong);

                        engine.play();

                        System.out.println(
                                "\n[Auto-play] Now playing: " +
                                        nextSong.getTitle()
                        );

                        System.out.print("> ");
                    }

                } catch (Exception e) {

                    System.out.println(
                            "\nReached end of playlist"
                    );

                    System.out.print("> ");
                }
            });
        });

        // Welcome Banner
        System.out.println(
                "-------- Welcome to Sonus 🎧 --------"
        );

        System.out.println(
                "Type 'help' to see commands"
        );

        // Main Loop
        while (true) {

            System.out.print("> ");

            String input = scanner.nextLine().trim();

            // Skip empty input
            if (input.isEmpty()) {
                continue;
            }

            // Exit
            if (input.equalsIgnoreCase("exit")) {

                try {

                    if (engine.getState() != null) {
                        engine.stop();
                    }

                } catch (Exception ignored) {
                }

                Platform.exit();

                scanner.close();

                System.out.println("Goodbye from Sonus 👋");

                break;
            }

            // Let command handler process commands
            boolean handled =
                    commandHandler.handle(input);

            // Unknown command fallback
            if (!handled) {

                System.out.println(
                        "Unknown command. Type 'help'"
                );
            }
        }
    }
}
