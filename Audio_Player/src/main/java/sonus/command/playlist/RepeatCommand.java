package sonus.command.playlist;

import sonus.command.Command;
import sonus.core.PlaylistManager;

public class RepeatCommand implements Command {

    private final PlaylistManager playlistManager;

    public RepeatCommand(
            PlaylistManager playlistManager
    ) {

        this.playlistManager =
                playlistManager;
    }

    @Override
    public boolean execute(String input) {

        if (!input.toLowerCase()
                .startsWith("repeat")) {

            return false;
        }

        // Prevent conflict with repeatsingle
        if (input.toLowerCase()
                .startsWith("repeatsingle")) {

            return false;
        }

        String[] parts =
                input.split("\\s+");

        // Toggle mode
        if (parts.length == 1) {

            boolean enabled =
                    !playlistManager
                            .isRepeatPlaylist();

            playlistManager
                    .setRepeatPlaylist(enabled);

            System.out.println(
                    "Repeat: " +
                            (enabled
                                    ? "ON"
                                    : "OFF")
            );

            return true;
        }

        // Explicit ON
        if (parts[1]
                .equalsIgnoreCase("on")) {

            playlistManager
                    .setRepeatPlaylist(true);

            System.out.println(
                    "Repeat: ON"
            );

            return true;
        }

        // Explicit OFF
        if (parts[1]
                .equalsIgnoreCase("off")) {

            playlistManager
                    .setRepeatPlaylist(false);

            System.out.println(
                    "Repeat: OFF"
            );

            return true;
        }

        System.out.println(
                "Usage: repeat on/off"
        );

        return true;
    }
}
