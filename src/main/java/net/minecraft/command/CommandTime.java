package net.minecraft.command;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

public class CommandTime extends CommandBase
{
    public String getCommandName()
    {
        return "time";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.time.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 1)
        {
            if (args[0].equals("set"))
            {
                int timeValue;

                if (args[1].equals("day"))
                {
                    timeValue = 1000;
                }
                else if (args[1].equals("night"))
                {
                    timeValue = 13000;
                }
                else
                {
                    timeValue = parseInt(args[1], 0);
                }

                this.setTime(sender, timeValue);
                notifyOperators(sender, this, "commands.time.set", new Object[] {Integer.valueOf(timeValue)});
                return;
            }

            if (args[0].equals("add"))
            {
                int addedTime = parseInt(args[1], 0);
                this.addTime(sender, addedTime);
                notifyOperators(sender, this, "commands.time.added", new Object[] {Integer.valueOf(addedTime)});
                return;
            }

            if (args[0].equals("query"))
            {
                if (args[1].equals("daytime"))
                {
                    int daytime = (int)(sender.getEntityWorld().getWorldTime() % 2147483647L);
                    sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, daytime);
                    notifyOperators(sender, this, "commands.time.query", new Object[] {Integer.valueOf(daytime)});
                    return;
                }

                if (args[1].equals("gametime"))
                {
                    int gameTime = (int)(sender.getEntityWorld().getTotalWorldTime() % 2147483647L);
                    sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, gameTime);
                    notifyOperators(sender, this, "commands.time.query", new Object[] {Integer.valueOf(gameTime)});
                    return;
                }
            }
        }

        throw new WrongUsageException("commands.time.usage", new Object[0]);
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"set", "add", "query"}): (args.length == 2 && args[0].equals("set") ? getListOfStringsMatchingLastWord(args, new String[] {"day", "night"}): (args.length == 2 && args[0].equals("query") ? getListOfStringsMatchingLastWord(args, new String[] {"daytime", "gametime"}): null));
    }

    protected void setTime(ICommandSender sender, int time)
    {
        for (int worldIndex = 0; worldIndex < MinecraftServer.getServer().worldServers.length; ++worldIndex)
        {
            MinecraftServer.getServer().worldServers[worldIndex].setWorldTime((long)time);
        }
    }

    protected void addTime(ICommandSender sender, int time)
    {
        for (int worldIndex = 0; worldIndex < MinecraftServer.getServer().worldServers.length; ++worldIndex)
        {
            WorldServer worldserver = MinecraftServer.getServer().worldServers[worldIndex];
            worldserver.setWorldTime(worldserver.getWorldTime() + (long)time);
        }
    }
}
