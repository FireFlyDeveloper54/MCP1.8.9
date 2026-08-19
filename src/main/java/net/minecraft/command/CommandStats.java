package net.minecraft.command;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CommandStats extends CommandBase
{
    public String getCommandName()
    {
        return "stats";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.stats.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.stats.usage", new Object[0]);
        }
        else
        {
            boolean blockMode;

            if (args[0].equals("entity"))
            {
                blockMode = false;
            }
            else
            {
                if (!args[0].equals("block"))
                {
                    throw new WrongUsageException("commands.stats.usage", new Object[0]);
                }

                blockMode = true;
            }

            int actionArgIndex;

            if (blockMode)
            {
                if (args.length < 5)
                {
                    throw new WrongUsageException("commands.stats.block.usage", new Object[0]);
                }

                actionArgIndex = 4;
            }
            else
            {
                if (args.length < 3)
                {
                    throw new WrongUsageException("commands.stats.entity.usage", new Object[0]);
                }

                actionArgIndex = 2;
            }

            String action = args[actionArgIndex++];

            if ("set".equals(action))
            {
                if (args.length < actionArgIndex + 3)
                {
                    if (actionArgIndex == 5)
                    {
                        throw new WrongUsageException("commands.stats.block.set.usage", new Object[0]);
                    }

                    throw new WrongUsageException("commands.stats.entity.set.usage", new Object[0]);
                }
            }
            else
            {
                if (!"clear".equals(action))
                {
                    throw new WrongUsageException("commands.stats.usage", new Object[0]);
                }

                if (args.length < actionArgIndex + 1)
                {
                    if (actionArgIndex == 5)
                    {
                        throw new WrongUsageException("commands.stats.block.clear.usage", new Object[0]);
                    }

                    throw new WrongUsageException("commands.stats.entity.clear.usage", new Object[0]);
                }
            }

            CommandResultStats.Type statType = CommandResultStats.Type.getTypeByName(args[actionArgIndex++]);

            if (statType == null)
            {
                throw new CommandException("commands.stats.failed", new Object[0]);
            }
            else
            {
                World world = sender.getEntityWorld();
                CommandResultStats resultStats;

                if (blockMode)
                {
                    BlockPos targetPos = parseBlockPos(sender, args, 1, false);
                    TileEntity tileEntity = world.getTileEntity(targetPos);

                    if (tileEntity == null)
                    {
                        throw new CommandException("commands.stats.noCompatibleBlock", new Object[] {Integer.valueOf(targetPos.getX()), Integer.valueOf(targetPos.getY()), Integer.valueOf(targetPos.getZ())});
                    }

                    if (tileEntity instanceof TileEntityCommandBlock)
                    {
                        resultStats = ((TileEntityCommandBlock)tileEntity).getCommandResultStats();
                    }
                    else
                    {
                        if (!(tileEntity instanceof TileEntitySign))
                        {
                            throw new CommandException("commands.stats.noCompatibleBlock", new Object[] {Integer.valueOf(targetPos.getX()), Integer.valueOf(targetPos.getY()), Integer.valueOf(targetPos.getZ())});
                        }

                        resultStats = ((TileEntitySign)tileEntity).getStats();
                    }
                }
                else
                {
                    Entity entity = getEntity(sender, args[1]);
                    resultStats = entity.getCommandStats();
                }

                if ("set".equals(action))
                {
                    String scoreHolderName = args[actionArgIndex++];
                    String objectiveName = args[actionArgIndex];

                    if (scoreHolderName.length() == 0 || objectiveName.length() == 0)
                    {
                        throw new CommandException("commands.stats.failed", new Object[0]);
                    }

                    CommandResultStats.setScoreBoardStat(resultStats, statType, scoreHolderName, objectiveName);
                    notifyOperators(sender, this, "commands.stats.success", new Object[] {statType.getTypeName(), objectiveName, scoreHolderName});
                }
                else if ("clear".equals(action))
                {
                    CommandResultStats.setScoreBoardStat(resultStats, statType, (String)null, (String)null);
                    notifyOperators(sender, this, "commands.stats.cleared", new Object[] {statType.getTypeName()});
                }

                if (blockMode)
                {
                    BlockPos targetPos = parseBlockPos(sender, args, 1, false);
                    TileEntity tileEntity = world.getTileEntity(targetPos);
                    tileEntity.markDirty();
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"entity", "block"}): (args.length == 2 && args[0].equals("entity") ? getListOfStringsMatchingLastWord(args, this.getAllUsernames()) : (args.length >= 2 && args.length <= 4 && args[0].equals("block") ? getTabCompletionCoordinate(args, 1, pos) : ((args.length != 3 || !args[0].equals("entity")) && (args.length != 5 || !args[0].equals("block")) ? ((args.length != 4 || !args[0].equals("entity")) && (args.length != 6 || !args[0].equals("block")) ? ((args.length != 6 || !args[0].equals("entity")) && (args.length != 8 || !args[0].equals("block")) ? null : getListOfStringsMatchingLastWord(args, this.getWritableObjectiveNames())) : getListOfStringsMatchingLastWord(args, CommandResultStats.Type.getTypeNames())) : getListOfStringsMatchingLastWord(args, new String[] {"set", "clear"}))));
    }

    protected String[] getAllUsernames()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }

    protected List<String> getWritableObjectiveNames()
    {
        Collection<ScoreObjective> collection = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard().getScoreObjectives();
        List<String> list = Lists.<String>newArrayList();

        for (ScoreObjective scoreobjective : collection)
        {
            if (!scoreobjective.getCriteria().isReadOnly())
            {
                list.add(scoreobjective.getName());
            }
        }

        return list;
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return args.length > 0 && args[0].equals("entity") && index == 1;
    }
}
