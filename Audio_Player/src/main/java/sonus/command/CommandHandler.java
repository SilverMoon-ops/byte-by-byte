package sonus.command;

import sonus.command.playback.PauseCommand;
import sonus.command.playback.PlayCommand;
import sonus.command.playback.StopCommand;
import sonus.command.system.HelpCommand;
import sonus.command.system.StatusCommand;
import sonus.command.playlist.ShuffleCommand;
import sonus.command.playlist.RepeatCommand;
import sonus.command.playlist.RepeatSingleCommand;
import sonus.command.playlist.FolderCommand;
import sonus.command.playlist.AddSongCommand;
import sonus.command.playlist.SeekCommand;
import sonus.command.playback.VolumeCommand;


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
                new ShuffleCommand(
                        playlistManager
                )
        );

        commands.add(
                new RepeatCommand(
                        playlistManager
                )
        );

        commands.add(
                new RepeatSingleCommand(
                        playlistManager
                )
        );

        commands.add(
                new FolderCommand(
                        playlistService
                )
        );

        commands.add(
                new AddSongCommand(
                        playlistService
                )
        );

        commands.add(
                new SeekCommand(engine)
        );

        commands.add(
                new VolumeCommand(engine)
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

        // COMMAND ALIASES

        switch (input.toLowerCase()) {

            case "p" -> input = "play";

            case "ps" -> input = "pause";

            case "s" -> input = "stop";

            case "n" -> input = "next";

            case "b" -> input = "previous";

            case "pl" -> input = "playlist";

            case "c" -> input = "current";

            case "h" -> input = "help";
        }

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




        // CLEAR PLAYLIST

        if (input.equalsIgnoreCase("clear")) {

            playlistManager.clearPlaylist();

            System.out.println(
                    "Playlist cleared"
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