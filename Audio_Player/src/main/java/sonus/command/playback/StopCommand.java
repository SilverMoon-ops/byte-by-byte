package sonus.command.playback;

import sonus.command.Command;
import sonus.core.AudioEngine;

public class StopCommand implements Command {

    private final AudioEngine engine;

    public StopCommand(
            AudioEngine engine
    ) {

        this.engine = engine;
    }

    @Override
    public boolean execute(String input) {

        if (!input.equalsIgnoreCase("stop")) {
            return false;
        }

        try {

            engine.stop();

        } catch (Exception e) {

            System.out.println(
                    "Error: " + e.getMessage()
            );
        }

        return true;
    }
}