package sonus.command.playlist;

import sonus.command.Command;
import sonus.service.PlaylistService;

public class FolderCommand implements Command {

    private final PlaylistService playlistService;

    public FolderCommand(
            PlaylistService playlistService
    ) {

        this.playlistService =
                playlistService;
    }

    @Override
    public boolean execute(String input) {

        if (!input.toLowerCase()
                .startsWith("folder ")) {

            return false;
        }

        try {

            String folderPath =
                    input.substring(7).trim();

            // Remove quotes
            if ((folderPath.startsWith("\"") &&
                    folderPath.endsWith("\"")) ||

                    (folderPath.startsWith("'") &&
                            folderPath.endsWith("'"))) {

                folderPath =
                        folderPath.substring(
                                1,
                                folderPath.length() - 1
                        );
            }

            int addedCount =
                    playlistService.addFolder(
                            folderPath
                    );

            System.out.println(
                    "Added " +
                            addedCount +
                            " song(s) to playlist"
            );

        } catch (Exception e) {

            System.out.println(
                    "Error: " +
                            e.getMessage()
            );
        }

        return true;
    }
}
