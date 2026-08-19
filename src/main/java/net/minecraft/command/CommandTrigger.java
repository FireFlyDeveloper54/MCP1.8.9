package net.minecraft.command;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandTrigger extends CommandBase
{
    public String getCommandName()
    {
        return "trigger";
    }

    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.trigger.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 3)
        {
            throw new WrongUsageException("commands.trigger.usage", new Object[0]);
        }
        else
        {
            EntityPlayerMP triggerPlayer;

            if (sender instanceof EntityPlayerMP)
            {
                triggerPlayer = (EntityPlayerMP)sender;
            }
            else
            {
                Entity entity = sender.getCommandSenderEntity();

                if (!(entity instanceof EntityPlayerMP))
                {
                    throw new CommandException("commands.trigger.invalidPlayer", new Object[0]);
                }

                triggerPlayer = (EntityPlayerMP)entity;
            }

            Scoreboard scoreboard = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
            ScoreObjective objective = scoreboard.getObjective(args[0]);

            if (objective != null && objective.getCriteria() == IScoreObjectiveCriteria.TRIGGER)
            {
                int triggerValue = parseInt(args[2]);

                if (!scoreboard.entityHasObjective(triggerPlayer.getName(), objective))
                {
                    throw new CommandException("commands.trigger.invalidObjective", new Object[] {args[0]});
                }
                else
                {
                    Score score = scoreboard.getValueFromObjective(triggerPlayer.getName(), objective);

                    if (score.isLocked())
                    {
                        throw new CommandException("commands.trigger.disabled", new Object[] {args[0]});
                    }
                    else
                    {
                        if ("set".equals(args[1]))
                        {
                            score.setScorePoints(triggerValue);
                        }
                        else
                        {
                            if (!"add".equals(args[1]))
                            {
                                throw new CommandException("commands.trigger.invalidMode", new Object[] {args[1]});
                            }

                            score.increseScore(triggerValue);
                        }

                        score.setLocked(true);

                        if (triggerPlayer.theItemInWorldManager.isCreative())
                        {
                            notifyOperators(sender, this, "commands.trigger.success", new Object[] {args[0], args[1], args[2]});
                        }
                    }
                }
            }
            else
            {
                throw new CommandException("commands.trigger.invalidObjective", new Object[] {args[0]});
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            Scoreboard scoreboard = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
            List<String> list = Lists.<String>newArrayList();

            for (ScoreObjective scoreObjective : scoreboard.getScoreObjectives())
            {
                if (scoreObjective.getCriteria() == IScoreObjectiveCriteria.TRIGGER)
                {
                    list.add(scoreObjective.getName());
                }
            }

            return getListOfStringsMatchingLastWord(args, (String[])list.toArray(new String[list.size()]));
        }
        else
        {
            return args.length == 2 ? getListOfStringsMatchingLastWord(args, new String[] {"add", "set"}): null;
        }
    }
}
