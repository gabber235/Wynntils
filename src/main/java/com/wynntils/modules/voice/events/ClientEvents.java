package com.wynntils.modules.voice.events;

import com.wynntils.Reference;
import com.wynntils.core.events.custom.ChatEvent;
import com.wynntils.core.events.custom.WynnClassChangeEvent;
import com.wynntils.core.events.custom.WynncraftServerEvent;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.modules.voice.configs.VoiceConfig;
import com.wynntils.modules.voice.managers.VoiceManager;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND;

public class ClientEvents implements Listener {
    @SubscribeEvent
    public void classChange(WynnClassChangeEvent e) {
        if (e.getCurrentClass() == ClassType.NONE) VoiceManager.stop();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void chat(ChatEvent.Pre e) {
        if (!VoiceConfig.INSTANCE.allowVoiceModule) return;
        String message = VoiceManager.formatChatMessage(e.getMessage().getUnformattedText());
        if (message == null) return;
        Supplier<ITextComponent> formattedMessage = !VoiceConfig.INSTANCE.addVoiceInteraction ? null : () -> e.getMessage().createCopy()
                .appendSibling(getAppendableText("!", "Report this voice line!", TextFormatting.GOLD,
                        "/voice report " + message))
                .appendSibling(getAppendableText("❤", "Like this voice line!", TextFormatting.BLUE,
                        "/voice like " + message));
        VoiceManager.playLine(message, e.getMessage(), formattedMessage);

        e.setChatLineId(e.getMessage().getUnformattedText().hashCode());
    }

    private ITextComponent getAppendableText(String text, String hover, TextFormatting color, String command) {
        assert text != null && hover != null && color != null && command != null;

        List<ITextComponent> components = new ArrayList<>();
        ITextComponent startBracket = new TextComponentString(" [");
        startBracket.getStyle().setColor(TextFormatting.DARK_GRAY);
        components.add(startBracket);
        ITextComponent time = new TextComponentString(text);
        time.getStyle().setColor(color);
        components.add(time);
        ITextComponent endBracket = new TextComponentString("]");
        endBracket.getStyle().setColor(TextFormatting.DARK_GRAY);
        components.add(endBracket);

        ITextComponent hoverText = new TextComponentString(hover);
        hoverText.getStyle().setColor(color).setBold(true);

        TextComponentBase base = new TextComponentString("");
        base.getSiblings().addAll(components);
        base.setStyle(new Style().setClickEvent(new ClickEvent(RUN_COMMAND, command)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
        return base;
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START || !Reference.onWorld) return;

        VoiceManager.getPlayer().updateController();
    }

    @SubscribeEvent
    public void serverLeft(WynncraftServerEvent.Leave e) {
        VoiceManager.stop();
    }
}
