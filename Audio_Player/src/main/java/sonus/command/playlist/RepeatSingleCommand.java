package sonus.command.playlist;

import sonus.command.Command;
import sonus.core.PlaylistManager;

public class RepeatSingleCommand
        implements Command {

    private final PlaylistManager playlistManager;

    public RepeatSingleCommand(
            PlaylistManager playlistManager
    ) {

        this.playlistManager =
                playlistManager;
    }

    @Override
    public boolean execute(String input) {

        if (!input.toLowerCase()
                .startsWith("repeatsingle")) {

            return false;
        }

        String[] parts =
                input.split("\\s+");

        // Toggle mode
        if (parts.length == 1) {

            boolean enabled =
                    !playlistManager
                            .isRepeatSingleSong();

            playlistManager
                    .setRepeatSingleSong(enabled);

            System.out.println(
                    "Repeat Single: " +
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
                    .setRepeatSingleSong(true);

            System.out.println(
                    "Repeat Single: ON"
            );

            return true;
        }

        // Explicit OFF
        if (parts[1]
                .equalsIgnoreCase("off")) {

            playlistManager
                    .setRepeatSingleSong(false);

            System.out.println(
                    "Repeat Single: OFF"
            );

            return true;
        }

        System.out.println(
                "Usage: repeatsingle on/off"
        );

        return true;
    }
}
