package sonus.command.playback;

import sonus.command.Command;
import sonus.core.AudioEngine;
import sonus.core.PlayerState;
import sonus.core.PlaylistManager;
import sonus.model.Song;

public class PlayCommand implements Command {

    private final AudioEngine engine;

    private final PlaylistManager playlistManager;

    public PlayCommand(
            AudioEngine engine,
            PlaylistManager playlistManager
    ) {

        this.engine = engine;

        this.playlistManager = playlistManager;
    }

    @Override
    public boolean execute(String input) {

        if (!input.equalsIgnoreCase("play")) {
            return false;
        }

        Song currentSong =
                playlistManager.getCurrentSong();

        if (currentSong == null) {

            System.out.println(
                    "Playlist is empty"
            );

            return true;
        }

        try {

            if (engine.getState() ==
                    PlayerState.PAUSED) {

                engine.play();

            } else {

                engine.load(currentSong);

                engine.play();
            }

        } catch (Exception e) {

            System.out.println(
                    "Error: " + e.getMessage()
            );
        }

        return true;
    }
}