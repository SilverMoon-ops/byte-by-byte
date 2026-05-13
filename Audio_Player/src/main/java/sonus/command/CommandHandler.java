package sonus.command;

import sonus.command.playback.PauseCommand;
import sonus.command.playback.PlayCommand;
import sonus.command.playback.StopCommand;
import sonus.command.system.HelpCommand;
import sonus.command.system.StatusCommand;

import sonus.core.JavaFXPlayerEngine;
import sonus.core.PlaylistManager;

import sonus.model.Song;

import sonus.service.PlaylistService;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    private final JavaFXPlayerEngine engine;

    private final PlaylistManager playlistManager;

    private final PlaylistService playlistService;

    private final List<Command> commands;

    public CommandHandler(
            JavaFXPlayerEngine engine,
            PlaylistManager playlistManager,
            PlaylistService playlistService
    ) {

        this.engine = engine;

        this.playlistManager = playlistManager;

        this.playlistService = playlistService;

        this.commands = new ArrayList<>();

        commands.add(new HelpCommand());

        commands.add(
                new PlayCommand(
                        engine,
                        playlistManager
                )
        );

        commands.add(
                new PauseCommand(engine)
        );

        commands.add(
                new StopCommand(engine)
        );

        commands.add(
                new StatusCommand(
                        engine,
                        playlistManager
                )
        );
    }

    public boolean handle(String input) {

        // Modular commands
        for (Command command : commands) {

            if (command.execute(input)) {
                return true;
            }
        }

        // CURRENT SONG
        if (input.equalsIgnoreCase("current")) {

            Song currentSong =
                    playlistManager.getCurrentSong();

            if (currentSong == null) {

                System.out.println(
                        "No current song"
                );

            } else {

                System.out.println(
                        "Current Song: " +
                                currentSong
                );
            }

            return true;
        }

        // VOLUME
        if (input.toLowerCase().startsWith("volume ")) {

            try {

                String value =
                        input.substring(7).trim();

                int volume =
                        Integer.parseInt(value);

                volume =
                        Math.max(
                                0,
                                Math.min(100, volume)
                        );

                engine.setVolume(volume);

                System.out.println(
                        "Volume set to " +
                                volume +
                                "%"
                );

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid volume number"
                );

            } catch (Exception e) {

                System.out.println(
                        "Error: " +
                                e.getMessage()
                );
            }

            return true;
        }

        // FOLDER
        if (input.toLowerCase().startsWith("folder ")) {

            try {

                String folderPath =
                        input.substring(7).trim();

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

        // PLAYLIST
        if (input.equalsIgnoreCase("playlist")) {

            playlistManager.showPlaylist();

            return true;
        }

        // NEXT
        if (input.equalsIgnoreCase("next")) {

            try {

                Song nextSong =
                        playlistManager.nextSong();

                engine.load(nextSong);

                engine.play();

            } catch (Exception e) {

                System.out.println(
                        "Error: " +
                                e.getMessage()
                );
            }

            return true;
        }

        // PREVIOUS
        if (input.equalsIgnoreCase("previous")) {

            try {

                Song previousSong =
                        playlistManager.previousSong();

                engine.load(previousSong);

                engine.play();

            } catch (Exception e) {

                System.out.println(
                        "Error: " +
                                e.getMessage()
                );
            }

            return true;
        }

        // SHUFFLE
        if (input.equalsIgnoreCase("shuffle")) {

            if (playlistManager.isShuffleEnabled()) {

                playlistManager.disableShuffle();

            } else {

                playlistManager.enableShuffle();
            }

            return true;
        }

        // REPEAT PLAYLIST
        if (input.equalsIgnoreCase("repeatplaylist")) {

            boolean enabled =
                    !playlistManager.isRepeatPlaylist();

            playlistManager.setRepeatPlaylist(
                    enabled
            );

            System.out.println(
                    "Repeat Playlist: " +
                            (enabled ? "ON" : "OFF")
            );

            return true;
        }

        // REMOVE
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
                        "Error: " +
                                e.getMessage()
                );
            }

            return true;
        }

        return false;
    }
}