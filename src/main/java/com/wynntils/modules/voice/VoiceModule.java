package com.wynntils.modules.voice;

import com.wynntils.core.framework.instances.Module;
import com.wynntils.core.framework.interfaces.annotations.ModuleInfo;
import com.wynntils.modules.voice.command.CommandVoice;
import com.wynntils.modules.voice.configs.VoiceConfig;
import com.wynntils.modules.voice.events.ClientEvents;

@ModuleInfo(name = "voice", displayName = "WynnVoiceOver")
public class VoiceModule extends Module {
    @Override
    public void onEnable() {
        registerSettings(VoiceConfig.class);
        registerEvents(new ClientEvents());
        registerCommand(new CommandVoice());
    }
}
