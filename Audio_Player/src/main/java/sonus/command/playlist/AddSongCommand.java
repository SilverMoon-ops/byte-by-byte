package sonus.command.playlist;

import sonus.command.Command;
import sonus.service.PlaylistService;

public class AddSongCommand implements Command {

    private final PlaylistService playlistService;

    public AddSongCommand(
            PlaylistService playlistService
    ) {

        this.playlistService =
                playlistService;
    }

    @Override
    public boolean execute(String input) {

        if (!input.toLowerCase()
                .startsWith("add ")) {

            return false;
        }

        try {

            String filePath =
                    input.substring(4).trim();

            // Remove quotes
            if ((filePath.startsWith("\"") &&
                    filePath.endsWith("\"")) ||

                    (filePath.startsWith("'") &&
                            filePath.endsWith("'"))) {

                filePath =
                        filePath.substring(
                                1,
                                filePath.length() - 1
                        );
            }

            playlistService.addSong(filePath);

        } catch (Exception e) {

            System.out.println(
                    "Error: " +
                            e.getMessage()
            );
        }

        return true;
    }
}
