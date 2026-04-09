package sonus.app;

import sonus.core.PlayerEngine;
import sonus.model.Song;

import java.util.Scanner;

public class MainApp {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        PlayerEngine engine = new PlayerEngine();

        System.out.println("Welcome to Sonus 🎧");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            try {
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                else if (input.toLowerCase().startsWith("load")) {

                    String filePath = input.substring(4).trim();

                    if (filePath.isEmpty()) {
                        System.out.println("Usage: load <filepath>");
                        continue;
                    }

                    // Remove quotes if present
                    if ((filePath.startsWith("\"") && filePath.endsWith("\"")) ||
                            (filePath.startsWith("'") && filePath.endsWith("'"))) {
                        filePath = filePath.substring(1, filePath.length() - 1);
                    }

                    Song song = new Song(filePath, filePath, "Unknown", "wav", 0);
                    engine.load(song);
                }

                else if (input.equalsIgnoreCase("play")) {
                    engine.play();
                }

                else if (input.equalsIgnoreCase("pause")) {
                    engine.pause();
                }

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