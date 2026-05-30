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
    private PlayerState state = PlayerState.STOPPED;
    private SourceDataLine speakers;
    private Thread playbackThread;
    private volatile boolean playing;
    private Runnable onSongFinished;
    private Runnable onProgressUpdate;
    private FloatControl volumeControl;
    private volatile double currentVolume = 1.0;
    private volatile double currentSpeed = 1.0;
    private volatile boolean speedChanged = false;// Default normal speed
    private final Object playbackLock = new Object();
    private volatile double lastReportedTime = 0.0;
    private volatile boolean isSeeking = false;
    private volatile double timestampOffset = 0.0;// ✅ Tracks the delta between target seek and FFmpeg packet position
    private volatile boolean seekOccurred = false;
    private volatile boolean seekRequested = false;
    private volatile double seekTargetSeconds = 0.0;

    private String detectFormat(String filePath) {
        try {
            int lastDot = filePath.lastIndexOf(".");
            if (lastDot > 0) {
                String extension = filePath.substring(lastDot + 1).toLowerCase().trim();
                return extension.isEmpty() ? null : extension;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public void load(Song song) {
        try {
            avutil.av_log_set_level(avutil.AV_LOG_FATAL);

            grabber = new FFmpegFrameGrabber(song.getFilePath());

            String format = detectFormat(song.getFilePath());
            if (format != null) {
                grabber.setFormat(format);
            }

            grabber.setOption("analyzeduration", "10000000");
            grabber.setOption("probesize", "5000000");
            grabber.start();
            grabber.setTimestamp(0);
            grabber.setTimestamp(0);

            // ✅ WAIT before getting channels (let FFmpeg initialize)
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            int channels = grabber.getAudioChannels();

            // ✅ RELEASE old filter before creating new one
            if (audioFilter != null) {
                try {
                    audioFilter.stop();
                    audioFilter.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                audioFilter = null;
            }

            // ✅ Create FRESH audio filter
            audioFilter = new FFmpegFrameFilter(
                    "atempo=" + currentSpeed + ",aformat=sample_fmts=s16:channel_layouts=" +
                            (channels == 1 ? "mono" : "stereo"),
                    channels
            );
            audioFilter.setSampleRate(grabber.getSampleRate());
            audioFilter.start();

            lastReportedTime = 0.0; // Reset tracking
            isSeeking = false;
            timestampOffset = 0.0; // Reset offset

            this.speedChanged = false;

            currentSong = song;
            state = PlayerState.STOPPED;

            System.out.println("Song loaded: " + song);
            System.out.println("Format: " + grabber.getFormat());
            System.out.println("Duration: " + grabber.getLengthInTime() / 1_000_000 + " sec");

        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException("Failed to load song", e);
        } catch (FrameFilter.Exception e) {
            throw new RuntimeException("Failed to initialize audio filter", e);
        }
    }

    @Override
    public void play() {
        synchronized (playbackLock) {
            // Resume playback
            if (state == PlayerState.PAUSED) {
                if (speakers != null) {
                    speakers.start(); // Start the audio hardware back up
                }
                playing = true;
                state = PlayerState.PLAYING;
                System.out.println("Playback resumed");
                return;
            }

            // Ignore duplicate play clicks
            if (state == PlayerState.PLAYING) {
                System.out.println("Song already playing");
                return;
            }

            // No song loaded
            if (grabber == null) {
                throw new RuntimeException("No song loaded");
            }

            playing = true;
            state = PlayerState.PLAYING;
        }

        playbackThread = new Thread(() -> {
            try {
                startPlaybackLoop();
            } catch (FrameGrabber.Exception | FrameFilter.Exception |
                     LineUnavailableException | InterruptedException e) {
                e.printStackTrace();

                synchronized (playbackLock) {
                    playing = false;
                    state = PlayerState.STOPPED;
                }
            }
        });

        playbackThread.setDaemon(true);
        playbackThread.start();
        System.out.println("Playing: " + currentSong);
    }

    private void startPlaybackLoop()
            throws FrameGrabber.Exception, FrameFilter.Exception,
            LineUnavailableException, InterruptedException {

        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                grabber.getSampleRate(),
                16,
                grabber.getAudioChannels(),
                grabber.getAudioChannels() * 2,
                grabber.getSampleRate(),
                false
        );

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);

            if (speakers.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) speakers.getControl(FloatControl.Type.MASTER_GAIN);
            }

            speakers.start();
            long lastProgressUpdate = 0;

            while (true) {
                if (Thread.currentThread().isInterrupted() || state == PlayerState.STOPPED) {
                    break;
                }

                // 🛑 PAUSE INTERCEPTOR: If paused, wait here until unpaused or stopped
                while (state == PlayerState.PAUSED) {
                    try {
                        Thread.sleep(20); // Sleep for 20ms to keep CPU usage at 0%
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    // If the user stops the song while paused, break out immediately
                    if (state == PlayerState.STOPPED) {
                        break;
                    }
                }

                // 1. SAFE ZONE: Handle Seek Requests Atomically on the Playback Thread
                if (seekRequested) {
                    synchronized (playbackLock) {
                        if (grabber != null) {
                            try {
                                long timestamp = (long) (seekTargetSeconds * 1_000_000);
                                grabber.setTimestamp(timestamp);

                                // Fully stop and close the old filter graph natively
                                if (audioFilter != null) {
                                    audioFilter.stop();
                                    audioFilter.close();
                                }

                                // Rebuild a clean filter instance on the correct thread
                                int channels = grabber.getAudioChannels();
                                audioFilter = new FFmpegFrameFilter(
                                        "atempo=" + currentSpeed + ",aformat=sample_fmts=s16:channel_layouts=" +
                                                (channels == 1 ? "mono" : "stereo"),
                                        channels
                                );
                                audioFilter.setSampleRate(grabber.getSampleRate());
                                audioFilter.start();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        seekRequested = false;
                    }
                    continue; // Jump straight to the next iteration to grab frames from the new position
                }

                if (speedChanged) {
                    synchronized (playbackLock) {
                        if (grabber != null) {
                            try {
                                if (audioFilter != null) {
                                    audioFilter.stop();
                                    audioFilter.close();
                                }
                                int channels = grabber.getAudioChannels();
                                audioFilter = new FFmpegFrameFilter(
                                        "atempo=" + currentSpeed + ",aformat=sample_fmts=s16:channel_layouts=" +
                                                (channels == 1 ? "mono" : "stereo"),
                                        channels
                                );
                                audioFilter.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        speedChanged = false; // Reset flag after successful rebuild
                    }
                }

                Frame rawFrame;
                synchronized (playbackLock) {
                    if (grabber == null) {
                        break;
                    }
                    rawFrame = grabber.grabSamples();

                    // ✅ Calculate precise post-seek offset immediately when the first fresh frame is read
                    if (isSeeking && rawFrame != null) {
                        double grabberTime = grabber.getTimestamp() / 1_000_000.0;
                        timestampOffset = grabberTime - lastReportedTime;
                        isSeeking = false;
                    }
                }

                if (rawFrame == null) {
                    break; // End of audio stream file reaching natural finish
                }

                if (rawFrame.samples == null) {
                    continue;
                }

                // 1. PUSH raw frames securely into the filter processing graph
                synchronized (playbackLock) {
                    if (audioFilter != null) {
                        audioFilter.push(rawFrame);
                    }
                }

                // 2. PULL modified, time-stretched frames out of the filter processing graph
                while (true) {
                    Frame filteredFrame;
                    synchronized (playbackLock) {
                        if (audioFilter == null) {
                            break;
                        }
                        filteredFrame = audioFilter.pull();
                    }

                    if (filteredFrame == null) {
                        break; // No more filtered frames available out of this chunk
                    }

                    if (filteredFrame.samples == null) {
                        continue;
                    }

                    // Process the processed, pitch-corrected sample sets directly
                    int availableBuffers = filteredFrame.samples.length;
                    ShortBuffer[] channelBuffers = new ShortBuffer[availableBuffers];
                    for (int ch = 0; ch < availableBuffers; ch++) {
                        channelBuffers[ch] = (ShortBuffer) filteredFrame.samples[ch];
                        channelBuffers[ch].rewind();
                    }

                    int samplesPerChannel = channelBuffers[0].remaining();
                    byte[] audioData = new byte[samplesPerChannel * availableBuffers * 2];
                    int index = 0;

                    for (int sampleIndex = 0; sampleIndex < samplesPerChannel; sampleIndex++) {
                        for (int ch = 0; ch < availableBuffers; ch++) {
                            short sample = channelBuffers[ch].get();
                            int scaledSample = (int) (sample * currentVolume);

                            if (scaledSample > 32767) {
                                scaledSample = 32767;
                            } else if (scaledSample < -32768) {
                                scaledSample = -32768;
                            }

                            short finalSample = (short) scaledSample;
                            audioData[index++] = (byte) (finalSample & 0xff);
                            audioData[index++] = (byte) ((finalSample >> 8) & 0xff);
                        }
                    }

                    int frameSize = format.getFrameSize();
                    int validBytes = audioData.length - (audioData.length % frameSize);

                    if (validBytes > 0) {
                        speakers.write(audioData, 0, validBytes);
                    }
                }

                // =====================================================================
                // ✅ UPDATED WORKER CALL: Smooth Offset Tracking & Monotonic Filtering
                // =====================================================================
                long now = System.currentTimeMillis();
                if (now - lastProgressUpdate >= 500) {
                    lastProgressUpdate = now;
                    if (onProgressUpdate != null) {
                        synchronized (playbackLock) {
                            if (grabber != null) {
                                // While seeking settles, keep lastReportedTime exactly at the user's targeted position
                                if (!isSeeking) {
                                    double grabberTime = grabber.getTimestamp() / 1_000_000.0;
                                    double correctedTime = grabberTime - timestampOffset;

                                    if (correctedTime > lastReportedTime) {
                                        lastReportedTime = correctedTime;
                                    }

                                    // Hard limit to avoid drifting past song duration bounds
                                    double duration = getTotalDuration();
                                    if (duration > 0 && lastReportedTime > duration) {
                                        lastReportedTime = duration;
                                    }
                                }
                            }
                        }
                        onProgressUpdate.run();
                    }
                }
            }

            if (playing && state != PlayerState.STOPPED) {
                if (speakers != null) {
                    try {
                        speakers.drain();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            boolean naturalFinish = playing;
            playing = false;
            state = PlayerState.STOPPED;

            if (naturalFinish && onSongFinished != null) {
                try {
                    onSongFinished.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } finally {
            if (speakers != null) {
                try {
                    speakers.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                speakers = null;
            }
        }
    }

    @Override
    public void pause() {
        synchronized (playbackLock) {
            if (state != PlayerState.PLAYING) {
                return;
            }

            // Keep playing = true so the thread loop stays alive!
            state = PlayerState.PAUSED;

            if (speakers != null) {
                speakers.stop();
                speakers.flush(); // Clear out residual audio data to prevent pop/buzzing sounds
            }

            System.out.println("Playback paused");
        }
    }
    @Override
    public void stop() {
        try {
            playing = false;
            state = PlayerState.STOPPED;

            if (speakers != null) {
                speakers.stop();
                speakers.flush();
            }

            if (playbackThread != null) {
                playbackThread.interrupt();
                try {
                    playbackThread.join(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                playbackThread = null;
            }

            if (speakers != null) {
                speakers.close();
                speakers = null;
            }

            synchronized (playbackLock) {
                if (audioFilter != null) {
                    try {
                        audioFilter.stop();
                        audioFilter.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    audioFilter = null;
                }

                if (grabber != null) {
                    try {
                        grabber.stop();
                        grabber.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    grabber = null;
                }
            }

            System.out.println("Playback stopped");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void seek(double seconds) {
        synchronized (playbackLock) {
            if (grabber == null) return;

            seekTargetSeconds = seconds;
            seekRequested = true;
            isSeeking = true;
            lastReportedTime = seconds;

            // Instantly dump old samples waiting in the soundcard hardware
            // to prevent overlapping audio/pops
            if (speakers != null) {
                speakers.flush();
            }

            System.out.println("Seek requested to " + seconds + " sec");
        }
    }

    @Override
    public void setVolume(double volume) {
        if (volume < 0) volume = 0;
        if (volume > 100) volume = 100;

        currentVolume = volume / 100.0;

        System.out.println(
                "Volume set to " + (int) volume +
                        "% (multiplier: " + String.format("%.2f", currentVolume) + ")"
        );
    }

    @Override
    public int getVolume() {
        return (int) (currentVolume * 100);
    }

    @Override
    public double getCurrentTime() {
        return lastReportedTime;
    }

    @Override
    public double getTotalDuration() {
        if (grabber == null) return 0;
        return grabber.getLengthInTime() / 1_000_000.0;
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
    public void setOnSongFinished(Runnable callback) {
        this.onSongFinished = callback;
    }

    @Override
    public void setOnProgressUpdate(Runnable callback) {
        this.onProgressUpdate = callback;
    }

    @Override
    public void setSpeed(double speed) {
        if (speed < 0.5) speed = 0.5;
        if (speed > 2.0) speed = 2.0;
        this.currentSpeed = speed;
        this.speedChanged = true;
    }

    @Override
    public double getSpeed() {
        return this.currentSpeed;
    }
}