package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;

public class CommandSaveAll extends CommandBase
{
    public String getCommandName()
    {
        return "save-all";
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.save.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        MinecraftServer minecraftserver = MinecraftServer.getServer();
        sender.addChatMessage(new ChatComponentTranslation("commands.save.start", new Object[0]));

        if (minecraftserver.getConfigurationManager() != null)
        {
            minecraftserver.getConfigurationManager().saveAllPlayerData();
        }

        try
        {
            for (int worldIndex = 0; worldIndex < minecraftserver.worldServers.length; ++worldIndex)
            {
                if (minecraftserver.worldServers[worldIndex] != null)
                {
                    WorldServer worldserver = minecraftserver.worldServers[worldIndex];
                    boolean wasLevelSavingDisabled = worldserver.disableLevelSaving;
                    worldserver.disableLevelSaving = false;
                    worldserver.saveAllChunks(true, (IProgressUpdate)null);
                    worldserver.disableLevelSaving = wasLevelSavingDisabled;
                }
            }

            if (args.length > 0 && "flush".equals(args[0]))
            {
                sender.addChatMessage(new ChatComponentTranslation("commands.save.flushStart", new Object[0]));

                for (int worldIndex = 0; worldIndex < minecraftserver.worldServers.length; ++worldIndex)
                {
                    if (minecraftserver.worldServers[worldIndex] != null)
                    {
                        WorldServer worldserver1 = minecraftserver.worldServers[worldIndex];
                        boolean wasLevelSavingDisabled = worldserver1.disableLevelSaving;
                        worldserver1.disableLevelSaving = false;
                        worldserver1.saveChunkData();
                        worldserver1.disableLevelSaving = wasLevelSavingDisabled;
                    }
                }

                sender.addChatMessage(new ChatComponentTranslation("commands.save.flushEnd", new Object[0]));
            }
        }
        catch (MinecraftException minecraftexception)
        {
            notifyOperators(sender, this, "commands.save.failed", new Object[] {minecraftexception.getMessage()});
            return;
        }

        notifyOperators(sender, this, "commands.save.success", new Object[0]);
    }
}
