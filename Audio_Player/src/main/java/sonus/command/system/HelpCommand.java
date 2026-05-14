package sonus.command.system;

import sonus.command.Command;

public class HelpCommand implements Command {

    @Override
    public boolean execute(String input) {

        if (!input.equalsIgnoreCase("help")) {
            return false;
        }

        System.out.println(
                "\n--- Aliases ---"
        );

        System.out.println(
                "p  -> play"
        );

        System.out.println(
                "ps -> pause"
        );

        System.out.println(
                "s  -> stop"
        );

        System.out.println(
                "n  -> next"
        );

        System.out.println(
                "b  -> previous"
        );

        System.out.println(
                "pl -> playlist"
        );

        System.out.println(
                "c  -> current"
        );

        System.out.println(
                "repeatsingle on/off -> Repeat current song"
        );

        System.out.println(
                "h  -> help"
        );

        return true;
    }
}