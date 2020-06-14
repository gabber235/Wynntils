package com.wynntils.modules.voice.command;

import com.wynntils.modules.voice.managers.VoiceManager;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;

import java.util.Arrays;

public class CommandVoice extends CommandBase implements IClientCommand {

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Override
    public String getName() {
        return "voice";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Interact with voice lines.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length <= 1) return;
        final String line = VoiceManager.formatChatMessage(Strings.join(Arrays.copyOfRange(args, 1, args.length), " "));

        if (line == null) return;
        if (args[0].equals("report")) {
            VoiceManager.reportLine(line);
            TextComponentString text = new TextComponentString("Successfully reported the voice line. Thanks for your feedback!");
            text.getStyle().setColor(TextFormatting.GOLD);
            sender.sendMessage(text);
        } else if (args[0].equals("like")) {
            VoiceManager.likeLine(line);
            TextComponentString text = new TextComponentString("Thanks for your feedback! We really appreciate it ❤");
            text.getStyle().setColor(TextFormatting.BLUE);
            sender.sendMessage(text);
        }
    }


    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
