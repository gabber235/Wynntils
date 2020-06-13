package com.wynntils.modules.voice.events;

import com.wynntils.core.events.custom.ChatEvent;
import com.wynntils.core.events.custom.WynnClassChangeEvent;
import com.wynntils.core.events.custom.WynncraftServerEvent;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.modules.voice.configs.VoiceConfig;
import com.wynntils.modules.voice.managers.VoiceManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientEvents implements Listener {
    @SubscribeEvent
    public void classChange(WynnClassChangeEvent e) {
        if (e.getCurrentClass() == ClassType.NONE) VoiceManager.stop();
    }

    @SubscribeEvent
    public void chat(ChatEvent.Post e) {
        if (!VoiceConfig.INSTANCE.allowVoiceModule) return;
        VoiceManager.filterChatMessage(e.getMessage().getUnformattedText());
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) return;

        VoiceManager.getPlayer().updateController();
    }

    @SubscribeEvent
    public void serverLeft(WynncraftServerEvent.Leave e) {
        VoiceManager.stop();
    }
}
