package com.wynntils.modules.voice.instances;

import com.wynntils.Reference;
import com.wynntils.modules.voice.configs.VoiceConfig;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import org.lwjgl.opengl.Display;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VoicePlayer {
    Thread voicePlayer;
    boolean active = false;

    float currentVolume = 1;
    String currentLine;
    ConcurrentLinkedQueue<String> nextLines = new ConcurrentLinkedQueue<>();
    AdvancedPlayer currentPlayer;

    public void play(String url) {
        if (currentLine != null && currentLine.equals(url) && !nextLines.contains(url)) return;

        nextLines.add(url);
        updateController();
    }

    public void stop() {
        if (!active || currentPlayer == null) return;

        currentPlayer.stop();
        active = false;
        currentLine = null;
        nextLines.clear();
    }

    private void checkForTheEnd() {
        if (!active) return;

        currentLine = null;
    }

    public void setVolume(float volume) {
        if (!active || currentPlayer == null) return;
        if (currentPlayer.getAudioDevice() == null) return;

        if (currentPlayer.getAudioDevice() instanceof JavaSoundAudioDevice) {
            JavaSoundAudioDevice dv = (JavaSoundAudioDevice) currentPlayer.getAudioDevice();
            dv.setLineGain(volume);
            currentVolume = volume;
            Reference.LOGGER.info("setCurrentValue to " + getCurrentVolume());
        }
    }

    public float getCurrentVolume() {
        return currentVolume;
    }

    public void updateController() {
        active = true;

        if (currentLine == null && !nextLines.isEmpty()) {
            currentLine = nextLines.poll();
            startReproduction();
        } else {
            if (getCurrentVolume() > getTargetVolume()) {
                setVolume(Math.max(getCurrentVolume() - 0.2f, (getTargetVolume())));
            } else if (getCurrentVolume() < (getTargetVolume())) {
                setVolume(Math.min(getCurrentVolume() + 0.2f, (getTargetVolume())));
            }
        }
    }

    private float getTargetVolume() {
        return Display.isActive() ? VoiceConfig.INSTANCE.baseVolume : VoiceConfig.INSTANCE.focusVolume;
    }

    @SuppressWarnings("deprecation")
    private void startReproduction() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            if (voicePlayer != null && voicePlayer.isAlive()) voicePlayer.stop();
        }

        voicePlayer = new Thread(() -> {
            try {
                InputStream inputStream = new URL(currentLine).openStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                currentPlayer = new AdvancedPlayer(bufferedInputStream);
                currentPlayer.setPlayBackListener(new PlaybackListener() {
                    public void playbackStarted(PlaybackEvent var1) {
                        setVolume(getTargetVolume());
                    }

                    public void playbackFinished(PlaybackEvent var1) {
                        checkForTheEnd();
                    }
                });

                currentPlayer.play();

                inputStream.close();
                bufferedInputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        voicePlayer.setName("Wynntils - Voice Reproducer");
        voicePlayer.start();
    }
}
