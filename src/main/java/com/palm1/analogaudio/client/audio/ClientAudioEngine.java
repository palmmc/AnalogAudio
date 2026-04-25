package com.palm1.analogaudio.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.item.CassetteData;
import com.palm1.analogaudio.client.gui.CassetteDeckScreen;

public class ClientAudioEngine {
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "AnalogAudio-Downloader");
        t.setDaemon(true);
        return t;
    });
    private static final Map<Object, RadioStreamer> PLAYING = new ConcurrentHashMap<>();
    private static final Set<Object> FAILED = Collections
            .newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<Object, Long> LAST_TICKED = new ConcurrentHashMap<>();
    private static volatile boolean active = true;

    static {
        EXECUTOR.scheduleAtFixedRate(() -> {
            if (!active)
                return;
            long now = System.currentTimeMillis();
            for (Map.Entry<Object, Long> entry : LAST_TICKED.entrySet()) {
                if (now - entry.getValue() > 2000) {
                    Object identity = entry.getKey();
                    AnalogAudio.LOGGER.warn("Streamer {} is stagnant. Force-killing ghost audio.",
                            identity);
                    stopRadio(identity);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static float getLoudness(Object identity) {
        RadioStreamer streamer = PLAYING.get(identity);
        return (streamer != null) ? streamer.getAmplitude() : 0f;
    }

    public static void tickRadio(Object identity, Vec3 pos, CassetteData data, long startTime,
            float volume, boolean looping) {
        if (!active || FAILED.contains(identity))
            return;

        LAST_TICKED.put(identity, System.currentTimeMillis());

        RadioStreamer streamer = PLAYING.computeIfAbsent(identity, posIdent -> {
            RadioStreamer s = new RadioStreamer();
            s.setCurrentUUID(data.uuid());
            if (!EXECUTOR.isShutdown() && active) {
                EXECUTOR.submit(() -> ClientAudioEngine.downloadAndPlay(identity, data, startTime));
            }
            return s;
        });

        if (Minecraft.getInstance().player != null) {
            streamer.setSettings(volume, looping);
            streamer.updatePosition(Minecraft.getInstance().level, pos, Minecraft.getInstance().player);

            if (!data.uuid().equals(streamer.getCurrentUUID())) {
                streamer.setCurrentUUID(data.uuid());
                EXECUTOR.submit(() -> ClientAudioEngine.downloadAndPlay(identity, data, startTime));
                return;
            }

            Path cachedFile = streamer.getCurrentFile();
            if (cachedFile != null) {
                streamer.play(cachedFile, startTime);
            }
        }
    }

    public static void stopRadio(Object identity) {
        RadioStreamer streamer = PLAYING.remove(identity);
        LAST_TICKED.remove(identity);
        if (streamer != null) {
            streamer.stop();
        }
        FAILED.remove(identity);
    }

    public static void stopAll(String trigger) {
        active = false;
        AnalogAudio.LOGGER.info("Force-stop triggered by {}. Active streams: {}", trigger, PLAYING.size());
        for (Object identity : PLAYING.keySet()) {
            AnalogAudio.LOGGER.info("Stopping and clearing identity {}", identity);
            stopRadio(identity);
        }
        PLAYING.clear();
        FAILED.clear();
        LAST_TICKED.clear();
        AnalogAudio.LOGGER.info("Audio clean successful.");
    }

    public static void prepareForSession() {
        active = true;
    }

    private static void downloadAndPlay(Object identity, CassetteData data, long startTime) {
        if (!active)
            return;
        AnalogAudio.LOGGER.info("Starting audio process for URL: {}", data.url());
        try {
            Path cacheDir = Minecraft.getInstance().gameDirectory.toPath().resolve("analogaudio_cache");
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
                AnalogAudio.LOGGER.info("Created cache directory: {}", cacheDir);
            }

            Path file = cacheDir.resolve(data.uuid() + ".ogg");
            if (!Files.exists(file)) {
                AnalogAudio.LOGGER.info("Downloading file to: {}", file);
                HttpURLConnection connection = (HttpURLConnection) URI.create(data.url()).toURL()
                        .openConnection();
                connection.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");

                int responseCode = connection.getResponseCode();
                AnalogAudio.LOGGER.info("HTTP Response Code: {}", responseCode);

                if (responseCode >= 200 && responseCode < 300) {
                    long contentLength = connection.getContentLengthLong();
                    AnalogAudio.LOGGER.info("Starting download of {} bytes...", contentLength);
                    try (InputStream in = connection.getInputStream();
                            OutputStream out = Files.newOutputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        long totalRead = 0;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                            totalRead += bytesRead;
                            if (contentLength > 0) {
                                float progress = (float) totalRead / contentLength;
                                displayActionBarProgress(progress);
                            }
                        }
                    }
                    AnalogAudio.LOGGER.info("Download complete.");
                    CassetteDeckScreen.statusMessage = Component
                            .translatable("gui.analogaudio.cassette_deck.status.write_success");
                    Minecraft.getInstance().player.displayClientMessage(Component.empty(),
                            true);
                } else {
                    CassetteDeckScreen.setStatus(
                            Component.translatable("gui.analogaudio.cassette_deck.status.error_code", responseCode),
                            CassetteDeckScreen.StatusType.ERROR, 100);
                    throw new IOException("Server returned HTTP " + responseCode);
                }
            } else {
                AnalogAudio.LOGGER.info("File already in cache: {}", file);
                CassetteDeckScreen.setStatus(
                        Component.translatable("gui.analogaudio.cassette_deck.status.write_success"),
                        CassetteDeckScreen.StatusType.SUCCESS, 100);
            }

            RadioStreamer streamer = PLAYING.get(identity);
            if (streamer != null) {
                streamer.play(file, startTime);
            } else {
                AnalogAudio.LOGGER.warn("No streamer found for identity: {}", identity);
            }
        } catch (Exception e) {
            AnalogAudio.LOGGER.error("Failed to download or play audio from {}: {}", data.url(),
                    e.getMessage());
            CassetteDeckScreen.setStatus(Component.translatable("gui.analogaudio.cassette_deck.status.download_fail"),
                    CassetteDeckScreen.StatusType.ERROR, 100);
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("§c")
                                .append(Component.translatable("gui.analogaudio.cassette_deck.status.download_fail")),
                        true);
            }
            e.printStackTrace();
            PLAYING.remove(identity);
            FAILED.add(identity);
        }
    }

    private static void displayActionBarProgress(float progress) {
        if (Minecraft.getInstance().player == null)
            return;

        int totalBars = 36;
        int completedBars = (int) (progress * totalBars);

        Component downloading = Component.translatable("gui.analogaudio.cassette_deck.status.downloading");

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < completedBars; i++)
            bar.append("|");
        String completed = bar.toString();

        bar = new StringBuilder();
        for (int i = completedBars; i < totalBars; i++)
            bar.append("|");
        String remaining = bar.toString();

        Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§f").append(downloading).append(" §r[§a")
                        .append(completed).append("§7")
                        .append(remaining).append("§r]"),
                true);
    }
}
