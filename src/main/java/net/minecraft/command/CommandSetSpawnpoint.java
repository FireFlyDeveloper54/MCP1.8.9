package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandSetSpawnpoint extends CommandBase
{
    public String getCommandName()
    {
        return "spawnpoint";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.spawnpoint.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 1 && args.length < 4)
        {
            throw new WrongUsageException("commands.spawnpoint.usage", new Object[0]);
        }
        else
        {
            EntityPlayerMP targetPlayer = args.length > 0 ? getPlayer(sender, args[0]) : getCommandSenderAsPlayer(sender);
            BlockPos spawnPos = args.length > 3 ? parseBlockPos(sender, args, 1, true) : targetPlayer.getPosition();

            if (targetPlayer.worldObj != null)
            {
                targetPlayer.setSpawnPoint(spawnPos, true);
                notifyOperators(sender, this, "commands.spawnpoint.success", new Object[] {targetPlayer.getName(), Integer.valueOf(spawnPos.getX()), Integer.valueOf(spawnPos.getY()), Integer.valueOf(spawnPos.getZ())});
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : (args.length > 1 && args.length <= 4 ? getTabCompletionCoordinate(args, 1, pos) : null);
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
