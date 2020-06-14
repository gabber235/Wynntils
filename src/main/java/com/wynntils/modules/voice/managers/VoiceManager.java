package com.wynntils.modules.voice.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.wynntils.ModCore;
import com.wynntils.modules.voice.configs.VoiceConfig;
import com.wynntils.modules.voice.instances.VoicePlayer;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class VoiceManager {
    private static final Pattern dialogueChatFilter = Pattern.compile("(\\[\\d\\/\\d\\]) .+: .+");
    private static final Executor executor = Executors.newCachedThreadPool();
    private static final VoicePlayer player = new VoicePlayer();
    private static final LoadingCache<String, String> cachedLines = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(CacheLoader.from(line -> {
                if (Objects.equals(line, "[1/1] Tasim: Hey, You! Come over here, we need to talk to you!"))
                    return "https://firebasestorage.googleapis.com/v0/b/wynntils-mc.appspot.com/o/test1.mp3?alt=media&token=079b19eb-71f1-48db-8512-a851ff7b471c";
                return "";
            }));

    public static VoicePlayer getPlayer() {
        return player;
    }

    public static String formatChatMessage(String message) {
        if (!message.matches(dialogueChatFilter.pattern())) return null;
        return message.replace(ModCore.mc().getSession().getUsername(), "You");
    }

    public static boolean playLine(String line) {
        if (!VoiceConfig.INSTANCE.allowVoiceModule) return false;
        try {
            String url = cachedLines.get(line);
            if (url.equals("")) return false;
            player.play(url);
            return true;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void likeLine(String line) {

    }

    public static void stop() {
        player.stop();
    }
}
