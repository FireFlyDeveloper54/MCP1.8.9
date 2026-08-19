package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSettings;

public class CommandPublishLocalServer extends CommandBase
{
    public String getCommandName()
    {
        return "publish";
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.publish.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        String port = MinecraftServer.getServer().shareToLAN(WorldSettings.GameType.SURVIVAL, false);

        if (port != null)
        {
            notifyOperators(sender, this, "commands.publish.started", new Object[] {port});
        }
        else
        {
            notifyOperators(sender, this, "commands.publish.failed", new Object[0]);
        }
    }
}
