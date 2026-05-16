package sonus.command.playlist;

import sonus.command.Command;
import sonus.core.AudioEngine;

public class SeekCommand implements Command {

    private final AudioEngine engine;

    public SeekCommand(
            AudioEngine engine
    ) {

        this.engine = engine;
    }

    @Override
    public boolean execute(String input) {

        if (!input.toLowerCase()
                .startsWith("seek ")) {

            return false;
        }

        try {

            String value =
                    input.substring(5).trim();

            double seconds =
                    Double.parseDouble(value);

            if (seconds < 0) {

                System.out.println(
                        "Seek time cannot be negative"
                );

                return true;
            }

            engine.seek(seconds);

            long minutes =
                    (long) seconds / 60;

            long remainingSeconds =
                    (long) seconds % 60;

            System.out.println(
                    "Seeked to: " +
                            String.format(
                                    "%d:%02d",
                                    minutes,
                                    remainingSeconds
                            )
            );

        } catch (NumberFormatException e) {

            System.out.println(
                    "Invalid seek value"
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
