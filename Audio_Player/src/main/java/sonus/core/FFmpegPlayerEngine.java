package sonus.core;

import sonus.model.Song;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import java.nio.ShortBuffer;


import org.bytedeco.ffmpeg.global.avutil;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameGrabber;

public class FFmpegPlayerEngine
        implements AudioEngine {

    private FFmpegFrameGrabber grabber;

    private FFmpegFrameFilter audioFilter;

    private Song currentSong;

    private PlayerState state =
            PlayerState.STOPPED;

    private SourceDataLine speakers;

    private Thread playbackThread;

    private volatile boolean playing;

    private Runnable onSongFinished;

    private Runnable onProgressUpdate;

    private FloatControl volumeControl;

    private int currentVolume = 100;

    private final Object playbackLock =
            new Object();

    @Override
    public void load(Song song) {

        try {

            avutil.av_log_set_level(avutil.AV_LOG_FATAL);


            stop();

            grabber =
                    new FFmpegFrameGrabber(
                            song.getFilePath()
                    );

            grabber.setOption(
                    "analyzeduration",
                    "10000000"
            );

            grabber.setOption(
                    "probesize",
                    "5000000"
            );

            grabber.setAudioChannels(2);


            grabber.start();

            audioFilter =
                    new FFmpegFrameFilter(

                            "aformat=sample_fmts=s16:"
                                    +
                                    "channel_layouts=stereo",

                            2
                    );

            audioFilter.setSampleRate(
                    grabber.getSampleRate()
            );

            audioFilter.start();

            currentSong = song;

            state = PlayerState.STOPPED;

            System.out.println(
                    "Song loaded: " + song
            );

            System.out.println(
                    "Format: "
                            + grabber.getFormat()
            );

            System.out.println(
                    "Duration: "
                            +
                            grabber.getLengthInTime()
                                    / 1_000_000
                            +
                            " sec"
            );

        } catch (FrameGrabber.Exception e) {

            throw new RuntimeException(
                    "Failed to load song",
                    e
            );
        } catch (FrameFilter.Exception e) {

            throw new RuntimeException(
                    "Failed to initialize audio filter",
                    e
            );
        }
    }

    @Override
    public void play() {

        if (state == PlayerState.PAUSED) {

            if (speakers != null) {
                speakers.start();
            }

            playing = true;

            state = PlayerState.PLAYING;

            System.out.println(
                    "Playback resumed"
            );

            return;
        }

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

        playbackThread =
                new Thread(() -> {

                    try {

                        startPlaybackLoop();

                    } catch (
                            FrameGrabber.Exception
                            |
                            FrameFilter.Exception
                            |
                            LineUnavailableException
                            |
                            InterruptedException e
                    ) {

                        e.printStackTrace();
                    }

                });

        playbackThread.start();

        System.out.println(
                "Playing: "
                        + currentSong
        );
    }

    private void startPlaybackLoop()
            throws
            FrameGrabber.Exception,
            FrameFilter.Exception,
            LineUnavailableException,
            InterruptedException {

        AudioFormat format =
                new AudioFormat(

                        AudioFormat.Encoding.PCM_SIGNED,

                        grabber.getSampleRate(),

                        16,

                        grabber.getAudioChannels(),

                        grabber.getAudioChannels() * 2,

                        grabber.getSampleRate(),

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

        if (
                speakers.isControlSupported(
                        FloatControl.Type.MASTER_GAIN
                )
        ) {

            volumeControl =
                    (FloatControl)
                            speakers.getControl(
                                    FloatControl.Type.MASTER_GAIN
                            );
        }

        speakers.start();

        playing = true;

        state = PlayerState.PLAYING;

        long lastProgressUpdate = 0;

        while (true) {

            if (!playing) {

                Thread.sleep(100);

                continue;
            }

            if (
                    Thread.currentThread()
                            .isInterrupted()
            ) {

                break;
            }

            Frame rawFrame;

            synchronized (playbackLock) {

                if (grabber == null) {
                    break;
                }

                rawFrame =
                        grabber.grabSamples();
            }

            if (rawFrame == null) {
                break;
            }

            audioFilter.push(rawFrame);

            Frame frame =
                    audioFilter.pullSamples();

            if (frame == null) {
                continue;
            }

            if (frame.samples == null) {
                continue;
            }

            ShortBuffer buffer =
                    (ShortBuffer)
                            frame.samples[0];

            buffer.rewind();

            byte[] audioData =
                    new byte[
                            buffer.remaining() * 2
                            ];

            int index = 0;

            while (buffer.hasRemaining()) {

                short sample =
                        buffer.get();

                audioData[index++] =
                        (byte)
                                (sample & 0xff);

                audioData[index++] =
                        (byte)
                                (
                                        (sample >> 8)
                                                & 0xff
                                );
            }

            int frameSize =
                    format.getFrameSize();

            int validBytes =
                    audioData.length
                            -
                            (
                                    audioData.length
                                            % frameSize
                            );

            if (validBytes > 0) {

                speakers.write(
                        audioData,
                        0,
                        validBytes
                );
            }

            if (
                    onProgressUpdate
                            != null
            ) {

                long now =
                        System.currentTimeMillis();

                if (
                        now - lastProgressUpdate
                                >= 500
                ) {

                    lastProgressUpdate = now;

                    if (
                            onProgressUpdate
                                    != null
                    ) {

                        onProgressUpdate.run();
                    }
                }
            }
        }

        boolean naturalFinish =
                playing;

        playing = false;

        state =
                PlayerState.STOPPED;

        if (
                naturalFinish
                        &&
                        onSongFinished
                                != null
        ) {

            onSongFinished.run();
        }
    }

    @Override
    public void pause() {

        if (state != PlayerState.PLAYING) {
            return;
        }

        playing = false;

        state = PlayerState.PAUSED;

        if (speakers != null) {
            speakers.stop();
        }

        System.out.println(
                "Playback paused"
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

            if (audioFilter != null) {

                audioFilter.stop();

                audioFilter.release();

                audioFilter = null;
            }

            synchronized (playbackLock) {

                if (grabber != null) {

                    grabber.stop();

                    grabber.release();

                    grabber = null;
                }
            }

            if (
                    previousState
                            != PlayerState.STOPPED
            ) {

                System.out.println(
                        "Playback stopped"
                );
            }

        } catch (
                FrameGrabber.Exception
                |
                FrameFilter.Exception e
        ) {

            e.printStackTrace();
        }
    }

    @Override
    public void seek(double seconds) {

        if (grabber == null) {
            return;
        }

        try {

            long timestamp =
                    (long)
                            (
                                    seconds
                                            * 1_000_000
                            );

            grabber.setTimestamp(
                    timestamp
            );

            if (audioFilter != null) {
                audioFilter.stop();
                audioFilter.release();
                audioFilter = null;
            }

            audioFilter = new FFmpegFrameFilter(
                    "aformat=sample_fmts=s16:channel_layouts=stereo",
                    2
            );
            audioFilter.setSampleRate(grabber.getSampleRate());
            audioFilter.start();

            System.out.println(
                    "Seeked to "
                            + seconds
                            + " sec"
            );

        } catch (FrameGrabber.Exception | FFmpegFrameFilter.Exception e) {

            throw new RuntimeException(
                    "Failed to seek",
                    e
            );
        }
    }

    @Override
    public void setVolume(
            double volume
    ) {

        if (volumeControl == null) {
            return;
        }

        float min =
                volumeControl.getMinimum();

        float max =
                volumeControl.getMaximum();

        float gain =
                (float)
                        (
                                min
                                        +
                                        (max - min)
                                                *
                                                (
                                                        volume
                                                                / 100.0
                                                )
                        );

        volumeControl.setValue(gain);

        currentVolume =
                (int) volume;

        System.out.println(
                "Volume set to "
                        +
                        currentVolume
                        +
                        "%"
        );
    }

    @Override
    public int getVolume() {

        return currentVolume;
    }

    @Override
    public double getCurrentTime() {

        if (grabber == null) {
            return 0;
        }

        return
                grabber.getTimestamp()
                        / 1_000_000.0;
    }

    @Override
    public double getTotalDuration() {

        if (grabber == null) {
            return 0;
        }

        return
                grabber.getLengthInTime()
                        / 1_000_000.0;
    }

    @Override
    public Song getCurrentSong() {

        return currentSong;
    }

    @Override
    public PlayerState getState() {

        return state;
    }

    @Override
    public void setOnSongFinished(
            Runnable callback
    ) {

        this.onSongFinished =
                callback;
    }

    @Override
    public void setOnProgressUpdate(
            Runnable callback
    ) {

        this.onProgressUpdate =
                callback;
    }
}