package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class CommandSaveOn extends CommandBase
{
    public String getCommandName()
    {
        return "save-on";
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.save-on.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        MinecraftServer minecraftserver = MinecraftServer.getServer();
        boolean changedSaving = false;

        for (int worldIndex = 0; worldIndex < minecraftserver.worldServers.length; ++worldIndex)
        {
            if (minecraftserver.worldServers[worldIndex] != null)
            {
                WorldServer worldserver = minecraftserver.worldServers[worldIndex];

                if (worldserver.disableLevelSaving)
                {
                    worldserver.disableLevelSaving = false;
                    changedSaving = true;
                }
            }
        }

        if (changedSaving)
        {
            notifyOperators(sender, this, "commands.save.enabled", new Object[0]);
        }
        else
        {
            throw new CommandException("commands.save-on.alreadyOn", new Object[0]);
        }
    }
}
