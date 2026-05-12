package sonus.command;

import sonus.core.JavaFXPlayerEngine;
import sonus.core.PlayerState;
import sonus.core.PlaylistManager;
import sonus.model.Song;
import sonus.service.PlaylistService;

public class CommandHandler {

    private final JavaFXPlayerEngine engine;

    private final PlaylistManager playlistManager;

    private final PlaylistService playlistService;

    public CommandHandler(
            JavaFXPlayerEngine engine,
            PlaylistManager playlistManager,
            PlaylistService playlistService
    ) {

        this.engine = engine;

        this.playlistManager = playlistManager;

        this.playlistService = playlistService;
    }
    public boolean handle(String input) {

        // HELP
        if (input.equalsIgnoreCase("help")) {

            System.out.println("\n======= SONUS COMMANDS =======");

            System.out.println("play              -> Play current song");

            System.out.println("pause             -> Pause playback");

            System.out.println("stop              -> Stop playback");

            System.out.println("next              -> Next song");

            System.out.println("previous          -> Previous song");

            System.out.println("status            -> Show player status");

            System.out.println("volume <0-100>    -> Set volume");

            System.out.println("playlist          -> Show playlist");

            System.out.println("shuffle           -> Toggle shuffle");

            System.out.println("repeatplaylist    -> Toggle repeat playlist");

            System.out.println("repeatsingle      -> Toggle repeat current song");

            System.out.println("current           -> Show current song");

            System.out.println("exit              -> Exit Sonus");

            System.out.println("================================\n");

            return true;
        }

        // STATUS
        if (input.equalsIgnoreCase("status")) {

            Song currentSong =
                    playlistManager.getCurrentSong();

            if (currentSong == null) {

                System.out.println("No song loaded");

                return true;
            }

            double current = engine.getCurrentTime();

            double total = engine.getTotalDuration();

            long currentMinutes = (long) current / 60;

            long currentSeconds = (long) current % 60;

            long totalMinutes = (long) total / 60;

            long totalSeconds = (long) total % 60;

            System.out.println("\n======= SONUS STATUS =======");

            System.out.println(
                    "Song: " + currentSong
            );

            System.out.println(
                    "State: " + engine.getState()
            );

            System.out.println(
                    "Time: " +
                            String.format(
                                    "%d:%02d / %d:%02d",
                                    currentMinutes,
                                    currentSeconds,
                                    totalMinutes,
                                    totalSeconds
                            )
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

            System.out.println("============================\n");

            return true;
        }

        // PLAY
        if (input.equalsIgnoreCase("play")) {

            Song currentSong =
                    playlistManager.getCurrentSong();

            if (currentSong == null) {

                System.out.println("Playlist is empty");

                return true;
            }

            try {

                if (engine.getState() == PlayerState.PAUSED) {

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

        // PAUSE
        if (input.equalsIgnoreCase("pause")) {

            try {

                engine.pause();

            } catch (Exception e) {

                System.out.println(
                        "Error: " + e.getMessage()
                );
            }

            return true;
        }

        // STOP
        if (input.equalsIgnoreCase("stop")) {

            try {

                engine.stop();

            } catch (Exception e) {

                System.out.println(
                        "Error: " + e.getMessage()
                );
            }

            return true;
        }

        // CURRENT SONG
        if (input.equalsIgnoreCase("current")) {

            Song currentSong =
                    playlistManager.getCurrentSong();

            if (currentSong == null) {

                System.out.println("No current song");

            } else {

                System.out.println(
                        "Current Song: " + currentSong
                );
            }

            return true;
        }

        // VOLUME
        if (input.toLowerCase().startsWith("volume ")) {

            try {

                String value =
                        input.substring(7).trim();

                int volume = Integer.parseInt(value);

                volume = Math.max(0, Math.min(100, volume));

                engine.setVolume(volume);

                System.out.println(
                        "Volume set to " + volume + "%"
                );

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid volume number"
                );

            } catch (Exception e) {

                System.out.println(
                        "Error: " + e.getMessage()
                );
            }

            return true;
        }

        if (input.toLowerCase().startsWith("folder ")) {

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
                        playlistService.addFolder(folderPath);

                System.out.println(
                        "Added " +
                                addedCount +
                                " song(s) to playlist"
                );

            } catch (Exception e) {

                System.out.println(
                        "Error: " + e.getMessage()
                );
            }

            return true;
        }

        // PLAYLIST COMMAND

        if (input.equalsIgnoreCase("playlist")) {

            playlistManager.showPlaylist();

            return true;
        }

        // NEXT COMMAND

        if (input.equalsIgnoreCase("next")) {

            try {

                Song nextSong =
                        playlistManager.nextSong();

                engine.load(nextSong);

                engine.play();

            } catch (Exception e) {

                System.out.println(
                        "Error: " + e.getMessage()
                );
            }

            return true;
        }

        // PREVIOUS COMMAND

        if (input.equalsIgnoreCase("previous")) {

            try {

                Song previousSong =
                        playlistManager.previousSong();

                engine.load(previousSong);

                engine.play();

            } catch (Exception e) {

                System.out.println(
                        "Error: " + e.getMessage()
                );
            }

            return true;
        }

        // SHUFFLE COMMAND

        if (input.equalsIgnoreCase("shuffle")) {

            if (playlistManager.isShuffleEnabled()) {

                playlistManager.disableShuffle();

            } else {

                playlistManager.enableShuffle();
            }

            return true;
        }

        // REPEAT PLAYLIST COMMAND

        if (input.equalsIgnoreCase("repeatplaylist")) {

            boolean enabled =
                    !playlistManager.isRepeatPlaylist();

            playlistManager.setRepeatPlaylist(enabled);

            System.out.println(
                    "Repeat Playlist: " +
                            (enabled ? "ON" : "OFF")
            );

            return true;
        }

        // REMOVE COMMAND

        if (input.toLowerCase().startsWith("remove ")) {

            try {

                String value =
                        input.substring(7).trim();

                int index =
                        Integer.parseInt(value);

                playlistManager.removeSong(index);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid number"
                );

            } catch (Exception e) {

                System.out.println(
                        "Error: " + e.getMessage()
                );
            }

            return true;
        }

        return false;
    }
}

