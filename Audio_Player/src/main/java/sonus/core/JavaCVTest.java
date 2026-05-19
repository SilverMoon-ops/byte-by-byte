package sonus.core;

import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;

public class JavaCVTest {

    public static void main(String[] args) {

        File audioFile =
                new File("D:/test/OST Summer_s Gone - Aurora by Vesky(MP3_128K).mp3");

        File[] files =
                audioFile.listFiles();

        if (files != null) {

            for (File file : files) {

                System.out.println(
                        file.getName()
                );
            }
        }

        System.out.println(
                audioFile.exists()
        );

        System.out.println(
                audioFile.getAbsolutePath()
        );

        try (
                FFmpegFrameGrabber grabber =
                        new FFmpegFrameGrabber(audioFile)
        ) {

            grabber.start();

            System.out.println(
                    "Format: " +
                            grabber.getFormat()
            );

            System.out.println(
                    "Duration: " +
                            grabber.getLengthInTime() / 1_000_000
                            + " seconds"
            );

            System.out.println(
                    "Channels: " +
                            grabber.getAudioChannels()
            );

            grabber.stop();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}