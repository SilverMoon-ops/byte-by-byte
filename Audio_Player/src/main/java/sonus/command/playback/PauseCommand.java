package sonus.command.playback;

import sonus.command.Command;
import sonus.core.AudioEngine;

public class PauseCommand implements Command {

    private final AudioEngine engine;

    public PauseCommand(
             AudioEngine engine
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