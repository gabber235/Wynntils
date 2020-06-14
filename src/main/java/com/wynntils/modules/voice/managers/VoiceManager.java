package com.wynntils.modules.voice.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.wynntils.ModCore;
import com.wynntils.modules.chat.configs.ChatConfig;
import com.wynntils.modules.chat.instances.ChatTab;
import com.wynntils.modules.chat.managers.TabManager;
import com.wynntils.modules.chat.overlays.ChatOverlay;
import com.wynntils.modules.voice.configs.VoiceConfig;
import com.wynntils.modules.voice.instances.VoicePlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class VoiceManager {
    private static final Pattern dialogueChatFilter = Pattern.compile("(\\[\\d\\/\\d\\]) .+: .+");
    private static final Pattern dialogueFormattedChatFilter = Pattern.compile("(\\[\\d\\/\\d\\]) .+: .+(\\[.\\].*){2}");
    private static final Executor executor = Executors.newCachedThreadPool();
    private static final VoicePlayer player = new VoicePlayer();
    private static final LoadingCache<String, String> cachedLines = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(CacheLoader.from(line -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (Objects.equals(line, "[1/1] Tasim: Hey, You! Come over here, we need to talk to you!"))
                    return "https://firebasestorage.googleapis.com/v0/b/wynntils-mc.appspot.com/o/test1.mp3?alt=media&token=079b19eb-71f1-48db-8512-a851ff7b471c";
                return "";
            }));

    public static VoicePlayer getPlayer() {
        return player;
    }

    public static String formatChatMessage(String message) {
        if (!message.matches(dialogueChatFilter.pattern()) || message.matches(dialogueFormattedChatFilter.pattern()))
            return null;
        return message.replace(ModCore.mc().getSession().getUsername(), "You");
    }

    public static void playLine(String line, ITextComponent rawMessage, Supplier<ITextComponent> formattedMessage) {
        if (!VoiceConfig.INSTANCE.allowVoiceModule) return;

        Optional<ChatTab> optionalTab = formattedMessage == null ? Optional.empty() :
                TabManager.getAvailableTabs().stream()
                        .sorted((tab1, tab2) -> tab1.isLowPriority() ? -1 : tab2.isLowPriority() ? 1 : 0)
                        .filter(tab -> tab.regexMatches(rawMessage)).findFirst();

        boolean counting = ChatConfig.INSTANCE.blockChatSpamFilter && optionalTab.map(tab -> tab.getLastMessage()
                .getFormattedText().equals(rawMessage.getFormattedText())).orElse(false);

        int lastAmount = optionalTab.map(ChatTab::getLastAmount).orElse(1);

        executor.execute(() -> {
            try {
                String url = cachedLines.get(line);
                if (url.length() == 0) return;
                player.play(url);
                if (formattedMessage == null) return;
                Thread.sleep(10L);
                // Update the chat line, so players can report and like a voice line.
                optionalTab.ifPresent(tab -> {
                    ITextComponent message = formattedMessage.get();
                    if (counting)
                        message = message.appendSibling(new TextComponentString(" [" + (lastAmount - 1) + "x]")
                                .setStyle(new Style().setColor(TextFormatting.GRAY)));

                    ChatOverlay.getChat()
                            .printChatMessageWithOptionalDeletion(message,
                                    rawMessage.getUnformattedText().hashCode());

                    // Set the original message as the last message, so we can block spam.
                    tab.updateLastMessageAndAmount(rawMessage, lastAmount + 1);
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public static void likeLine(String line) {

    }

    public static void stop() {
        player.stop();
    }
}
