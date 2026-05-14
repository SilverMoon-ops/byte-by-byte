package sonus.command.playlist;

import sonus.command.Command;
import sonus.core.PlaylistManager;

public class ShuffleCommand implements Command {

    private final PlaylistManager playlistManager;

    public ShuffleCommand(
            PlaylistManager playlistManager
    ) {

        this.playlistManager =
                playlistManager;
    }

    @Override
    public boolean execute(String input) {

        if (!input.toLowerCase()
                .startsWith("shuffle")) {

            return false;
        }

        String[] parts =
                input.split("\\s+");

        // Toggle mode
        if (parts.length == 1) {

            if (playlistManager
                    .isShuffleEnabled()) {

                playlistManager
                        .disableShuffle();

                System.out.println(
                        "Shuffle: OFF"
                );

            } else {

                playlistManager
                        .enableShuffle();

                System.out.println(
                        "Shuffle: ON"
                );
            }

            return true;
        }

        // Explicit ON
        if (parts[1]
                .equalsIgnoreCase("on")) {

            playlistManager.enableShuffle();

            System.out.println(
                    "Shuffle: ON"
            );

            return true;
        }

        // Explicit OFF
        if (parts[1]
                .equalsIgnoreCase("off")) {

            playlistManager.disableShuffle();

            System.out.println(
                    "Shuffle: OFF"
            );

            return true;
        }

        System.out.println(
                "Usage: shuffle on/off"
        );

        return true;
    }
}
