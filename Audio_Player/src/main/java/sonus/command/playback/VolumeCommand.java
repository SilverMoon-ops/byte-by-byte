package sonus.command.playback;

import sonus.command.Command;
import sonus.core.JavaFXPlayerEngine;

public class VolumeCommand implements Command {

    private final JavaFXPlayerEngine engine;

    public VolumeCommand(
            JavaFXPlayerEngine engine
    ) {

        this.engine = engine;
    }

    @Override
    public boolean execute(String input) {

        if (!input.toLowerCase()
                .startsWith("volume ")) {

            return false;
        }

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
}
