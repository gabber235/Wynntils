package com.wynntils.modules.voice.configs;

import com.wynntils.Reference;
import com.wynntils.core.framework.settings.annotations.Setting;
import com.wynntils.core.framework.settings.annotations.SettingsInfo;
import com.wynntils.core.framework.settings.instances.SettingsClass;
import com.wynntils.modules.voice.managers.VoiceManager;

@SettingsInfo(name = "voice", displayPath = "Voice")
public class VoiceConfig extends SettingsClass {
    public static VoiceConfig INSTANCE;

    @Setting(displayName = "Voice Over", description = "Should voice-overs be played when a npc talks?", order = 0)
    public boolean allowVoiceModule = false;

    @Setting(displayName = "Base Volume", description = "How loud should all soundtracks be?")
    @Setting.Limitations.FloatLimit(max = 1f, min = -50f, precision = 1f)
    public float baseVolume = 1;

    @Setting(displayName = "Off Focus Volume", description = "How loud should the soundtrack be when Minecraft is not focused on?")
    @Setting.Limitations.FloatLimit(max = 1f, min = -50f, precision = 1f)
    public float focusVolume = -10;

    @Override
    public void onSettingChanged(String name) {
        if (!allowVoiceModule && Reference.onWorld) VoiceManager.stop();
    }
}
