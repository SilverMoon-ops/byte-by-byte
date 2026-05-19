package sonus.core;

import sonus.model.Song;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;
import org.bytedeco.javacv.Frame;
import java.nio.ShortBuffer;
import org.bytedeco.ffmpeg.global.avutil;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

public class FFmpegPlayerEngine
        implements AudioEngine {

    private FFmpegFrameGrabber grabber;

    private Song currentSong;

    private PlayerState state = PlayerState.STOPPED;

    private SourceDataLine speakers;

    private Thread playbackThread;

    private volatile boolean playing;

    @Override
    public void load(Song song) {

        try {

            stop();

            grabber =
                    new FFmpegFrameGrabber(
                            song.getFilePath()
                    );
            grabber.setSampleFormat(
                    avutil.AV_SAMPLE_FMT_S16
            );

            grabber.start();

            currentSong = song;

            state = PlayerState.STOPPED;

            System.out.println(
                    "Song loaded: " + song
            );

            System.out.println(
                    "Format: " +
                            grabber.getFormat()
            );

            System.out.println(
                    "Duration: " +
                            grabber.getLengthInTime()
                                    / 1_000_000 +
                            " sec"
            );

        } catch (FrameGrabber.Exception e) {

            throw new RuntimeException(
                    "Failed to load song",
                    e
            );
        }
    }

    @Override
    public void setVolume(double volume) {

        System.out.println(
                "Volume control not implemented yet"
        );
    }

    @Override
    public int getVolume() {
        return 0;
    }

    @Override
    public double getCurrentTime() {
        return 0;
    }

    @Override
    public double getTotalDuration() {
        return 0;
    }

    @Override
    public Song getCurrentSong() {
        return null;
    }

    @Override
    public void seek(double seconds) {

        System.out.println(
                "Seek control not implemented yet"
        );
    }

    @Override
    public void play() {

        if (grabber == null) {

            throw new RuntimeException(
                    "No song loaded"
            );
        }

        if (state == PlayerState.PLAYING) {

            System.out.println(
                    "Song already playing"
            );

            return;
        }

        playbackThread = new Thread(

                () -> {

            try {

                AudioFormat format =
                        new AudioFormat(
                                grabber.getSampleRate(),
                                16,
                                grabber.getAudioChannels(),
                                true,
                                false
                        );

                DataLine.Info info =
                        new DataLine.Info(
                                SourceDataLine.class,
                                format
                        );

                speakers =
                        (SourceDataLine)
                                AudioSystem.getLine(info);

                speakers.open(format);

                speakers.start();

                playing = true;

                state = PlayerState.PLAYING;

                Frame frame = null;

                while (
                        playing
                                &&
                                (frame = grabber.grabSamples()) != null
                )

                {

                    if (frame.samples == null) {
                        continue;
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    ShortBuffer buffer =
                            (ShortBuffer)
                                    frame.samples[0];

                    buffer.rewind();

                    byte[] audioData =
                            new byte[
                                    buffer.remaining() * 2
                                    ];

                    int i = 0;

                    while (buffer.hasRemaining()) {

                        short sample =
                                buffer.get();

                        audioData[i++] =
                                (byte) (sample & 0xff);

                        audioData[i++] =
                                (byte)
                                        ((sample >> 8) & 0xff);
                    }

                    speakers.write(
                            audioData,
                            0,
                            audioData.length
                    );
                }

                playing = false;

                state = PlayerState.STOPPED;

            } catch (Exception e) {

                e.printStackTrace();
            }

        });

        playbackThread.start();

        System.out.println(
                "Playing: " + currentSong
        );
    }

    @Override
    public void pause() {

        System.out.println(
                "Pause not implemented yet"
        );
    }

    @Override
    public void stop() {

        try {

            PlayerState previousState =
                    state;

            playing = false;

            state = PlayerState.STOPPED;

            if (playbackThread != null) {

                playbackThread.interrupt();

                playbackThread = null;
            }

            if (speakers != null) {

                if (speakers.isRunning()) {
                    speakers.stop();
                }

                speakers.flush();

                speakers.close();

                speakers = null;
            }

            if (grabber != null) {

                grabber.stop();

                grabber.release();

                grabber = null;
            }

            if (
                    previousState
                            != PlayerState.STOPPED
            ) {

                System.out.println(
                        "Playback stopped"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    //@Override
    public void next() {

        System.out.println(
                "Next not implemented yet"
        );
    }

    //@Override
    public void previous() {

        System.out.println(
                "Previous not implemented yet"
        );
    }

    @Override
    public PlayerState getState() {

        return state;
    }

    @Override
    public void setOnSongFinished(Runnable callback) {

    }

    @Override
    public void setOnProgressUpdate(Runnable callback) {

    }
}