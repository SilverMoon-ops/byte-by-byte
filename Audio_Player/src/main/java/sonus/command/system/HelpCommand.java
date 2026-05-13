package sonus.command.system;

import sonus.command.Command;

public class HelpCommand implements Command {

    @Override
    public boolean execute(String input) {

        if (!input.equalsIgnoreCase("help")) {
            return false;
        }

        System.out.println(
                "\n======= SONUS COMMANDS ======="
        );

        System.out.println(
                "play              -> Play current song"
        );

        System.out.println(
                "pause             -> Pause playback"
        );

        System.out.println(
                "stop              -> Stop playback"
        );

        System.out.println(
                "next              -> Next song"
        );

        System.out.println(
                "previous          -> Previous song"
        );

        System.out.println(
                "status            -> Show player status"
        );

        System.out.println(
                "volume <0-100>    -> Set volume"
        );

        System.out.println(
                "playlist          -> Show playlist"
        );

        System.out.println(
                "shuffle           -> Toggle shuffle"
        );

        System.out.println(
                "repeatplaylist    -> Toggle repeat playlist"
        );

        System.out.println(
                "repeatsingle      -> Toggle repeat current song"
        );

        System.out.println(
                "current           -> Show current song"
        );

        System.out.println(
                "exit              -> Exit Sonus"
        );

        System.out.println(
                "================================\n"
        );

        return true;
    }
}