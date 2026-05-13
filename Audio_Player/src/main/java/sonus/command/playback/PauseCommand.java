package sonus.command.playback;

import sonus.command.Command;
import sonus.core.JavaFXPlayerEngine;

public class PauseCommand implements Command {

    private final JavaFXPlayerEngine engine;

    public PauseCommand(
            JavaFXPlayerEngine engine
    ) {

        this.engine = engine;
    }

    @Override
    public boolean execute(String input) {

        if (!input.equalsIgnoreCase("pause")) {
            return false;
        }

        try {

            engine.pause();

        } catch (Exception e) {

            System.out.println(
                    "Error: " + e.getMessage()
            );
        }

        return true;
    }
}