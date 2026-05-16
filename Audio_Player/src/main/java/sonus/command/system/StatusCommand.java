package sonus.command.system;

import sonus.command.Command;
import sonus.core.AudioEngine;
import sonus.core.PlaylistManager;
import sonus.model.Song;

public class StatusCommand implements Command {

    private final AudioEngine engine;

    private final PlaylistManager playlistManager;

    public StatusCommand(
            AudioEngine engine,
            PlaylistManager playlistManager
    ) {

        this.engine = engine;

        this.playlistManager = playlistManager;
    }

    @Override
    public boolean execute(String input) {

        if (!input.equalsIgnoreCase("status")) {
            return false;
        }

        Song currentSong =
                playlistManager.getCurrentSong();

        if (currentSong == null) {

            System.out.println(
                    "No song loaded"
            );

            return true;
        }

        double currentTime =
                engine.getCurrentTime();

        double totalTime =
                engine.getTotalDuration();

        long currentMinutes =
                (long) currentTime / 60;

        long currentSeconds =
                (long) currentTime % 60;

        long totalMinutes =
                (long) totalTime / 60;

        long totalSeconds =
                (long) totalTime % 60;

        System.out.println(
                "\n======= SONUS STATUS ======="
        );

        System.out.println(
                "Song: " + currentSong
        );

        System.out.println(
                "State: " + engine.getState()
        );

        System.out.printf(
                "Time: %d:%02d / %d:%02d%n",
                currentMinutes,
                currentSeconds,
                totalMinutes,
                totalSeconds
        );

        System.out.println(
                "Volume: " +
                        engine.getVolume() + "%"
        );

        System.out.println(
                "Shuffle: " +
                        (playlistManager.isShuffleEnabled()
                                ? "ON"
                                : "OFF")
        );

        System.out.println(
                "Repeat Playlist: " +
                        (playlistManager.isRepeatPlaylist()
                                ? "ON"
                                : "OFF")
        );

        System.out.println(
                "Repeat Single: " +
                        (playlistManager.isRepeatSingleSong()
                                ? "ON"
                                : "OFF")
        );

        System.out.println(
                "==============================\n"
        );

        return true;
    }
}
