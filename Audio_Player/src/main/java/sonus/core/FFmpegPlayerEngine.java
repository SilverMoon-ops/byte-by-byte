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
    private final Object playbackLock = new Object();

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
                    "aformat=sample_fmts=s16:channel_layouts=" +
                            (channels == 1 ? "mono" : "stereo"),
                    channels
            );
            audioFilter.setSampleRate(grabber.getSampleRate());
            audioFilter.start();

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
        // 1. THIS IS THE KEY UNIFYING LOCK THAT MAKES IT SNIPPET 1
        synchronized (playbackLock) {
            // Resume playback
            if (state == PlayerState.PAUSED) {
                if (speakers != null) {
                    speakers.start();
                }
                playing = true;
                state = PlayerState.PLAYING;
                System.out.println("Playback resumed");
                return;
            }

            // Ignore duplicate play clicks
            if (playing) {
                System.out.println("Song already playing");
                return;
            }

            // No song loaded
            if (grabber == null) {
                throw new RuntimeException("No song loaded");
            }

            playing = true;
            state = PlayerState.PLAYING;
        } // 2. LOCK ENDS HERE

        playbackThread = new Thread(() -> {
            try {
                startPlaybackLoop();
            } catch (FrameGrabber.Exception | FrameFilter.Exception |
                     LineUnavailableException | InterruptedException e) {
                e.printStackTrace();

                // 3. ALSO SNIPPET 1: It re-locks to safely handle errors
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

            // --- ALL OF YOUR ORIGINAL DECODING CODE IS INSIDE HERE ---
            while (true) {

                if (Thread.currentThread().isInterrupted() || state == PlayerState.STOPPED) {
                    break;
                }

                if (!playing) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        break;
                    }
                    continue;
                }

                Frame rawFrame;

                synchronized (playbackLock) {
                    if (grabber == null) {
                        break;
                    }
                    rawFrame = grabber.grabSamples();
                }

                if (rawFrame == null) {
                    break;
                }

                if (rawFrame.samples == null) {
                    continue;
                }

                int availableBuffers = rawFrame.samples.length;
                byte[] audioData = new byte[0];

                if (rawFrame.samples.length > 0) {
                    ShortBuffer firstBuffer = (ShortBuffer) rawFrame.samples[0];
                    firstBuffer.rewind();
                    int samplesPerChannel = firstBuffer.remaining();
                    audioData = new byte[samplesPerChannel * availableBuffers * 2];
                }

                ShortBuffer[] channelBuffers = new ShortBuffer[availableBuffers];
                for (int ch = 0; ch < availableBuffers; ch++) {
                    channelBuffers[ch] = (ShortBuffer) rawFrame.samples[ch];
                    channelBuffers[ch].rewind();
                }

                int samplesPerChannel = channelBuffers[0].remaining();
                audioData = new byte[samplesPerChannel * availableBuffers * 2];
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

                long now = System.currentTimeMillis();
                if (now - lastProgressUpdate >= 500) {
                    lastProgressUpdate = now;
                    if (onProgressUpdate != null) {
                        onProgressUpdate.run();
                    }
                }
            } // --- END OF WHILE LOOP ---

            // Smooth transition handling
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
            // This is the safety net from Snippet 2 that guarantees clean closure!
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
        if (state != PlayerState.PLAYING) {
            return;
        }

        playing = false;
        state = PlayerState.PAUSED;

        if (speakers != null) {
            speakers.stop();
        }

        System.out.println("Playback paused");
    }

    @Override
    public void stop() {
        try {
            playing = false;
            state = PlayerState.STOPPED;

            // 1. UNBLOCK THE AUDIO HARDWARE FIRST!
            // This instantly cancels any blocked speakers.write() operations.
            if (speakers != null) {
                speakers.stop();
                speakers.flush();
            }

            // 2. SAFELY KILL THE THREAD
            if (playbackThread != null) {
                playbackThread.interrupt();
                try {
                    // Because we flushed the speakers above, this join will
                    // succeed instantly instead of timing out.
                    playbackThread.join(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                playbackThread = null;
            }

            // 3. SECURELY CLOSE THE LINE
            // The thread is guaranteed dead now, so closing is safe.
            if (speakers != null) {
                speakers.close();
                speakers = null;
            }

            // 4. CLEAN UP FFMPEG POINTERS
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
        if (grabber == null) {
            return;
        }

        int channels;
        int sampleRate; // Changed type to int to match what setSampleRate expects

        // --- FIRST LOCK: Jump timestamp and release old filter ---
        synchronized (playbackLock) {
            try {
                long timestamp = (long) (seconds * 1_000_000);
                grabber.setTimestamp(timestamp);

                if (speakers != null) {
                    speakers.flush();
                }

                if (audioFilter != null) {
                    audioFilter.stop();
                    audioFilter.release();
                    audioFilter = null;
                }

                // Save these variables while locked; cast sample rate to int
                channels = grabber.getAudioChannels();
                sampleRate = (int) grabber.getSampleRate();

            } catch (FrameGrabber.Exception | FrameFilter.Exception e) {
                throw new RuntimeException("Failed to initiate seek", e);
            }
        } // Lock is released here!

        // --- SAFE ZONE: Sleep happens here while other threads can run ---
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // --- SECOND LOCK: Safely rebuild the audio filter ---
        synchronized (playbackLock) {
            // Double-check grabber wasn't closed or nullified during the sleep
            if (grabber == null) {
                return;
            }

            try {
                audioFilter = new FFmpegFrameFilter(
                        "aformat=sample_fmts=s16:channel_layouts=" +
                                (channels == 1 ? "mono" : "stereo"),
                        channels
                );
                audioFilter.setSampleRate(sampleRate); // This will compile perfectly now!
                audioFilter.start();

                System.out.println("Seeked to " + seconds + " sec");

            } catch (FrameFilter.Exception e) {
                throw new RuntimeException("Failed to finalize seek filter", e);
            }
        }
    }

    @Override
    public void setVolume(double volume) {
        // ✅ Validate and convert 0-100 to 0.0-1.0
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
        if (grabber == null) return 0;
        return grabber.getTimestamp() / 1_000_000.0;
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
}