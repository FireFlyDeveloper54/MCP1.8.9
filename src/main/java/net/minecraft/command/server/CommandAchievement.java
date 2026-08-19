package net.minecraft.command.server;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;

public class CommandAchievement extends CommandBase
{
    public String getCommandName()
    {
        return "achievement";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.achievement.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException("commands.achievement.usage", new Object[0]);
        }
        else
        {
            final StatBase targetStat = StatList.getOneShotStat(args[1]);

            if (targetStat == null && !args[1].equals("*"))
            {
                throw new CommandException("commands.achievement.unknownAchievement", new Object[] {args[1]});
            }
            else
            {
                final EntityPlayerMP targetPlayer = args.length >= 3 ? getPlayer(sender, args[2]) : getCommandSenderAsPlayer(sender);
                boolean isGiving = args[0].equalsIgnoreCase("give");
                boolean isTaking = args[0].equalsIgnoreCase("take");

                if (isGiving || isTaking)
                {
                    if (targetStat == null)
                    {
                        if (isGiving)
                        {
                            for (Achievement achievement : AchievementList.achievementList)
                            {
                                targetPlayer.triggerAchievement(achievement);
                            }

                            notifyOperators(sender, this, "commands.achievement.give.success.all", new Object[] {targetPlayer.getName()});
                        }
                        else if (isTaking)
                        {
                            for (Achievement achievement : Lists.reverse(AchievementList.achievementList))
                            {
                                targetPlayer.resetStat(achievement);
                            }

                            notifyOperators(sender, this, "commands.achievement.take.success.all", new Object[] {targetPlayer.getName()});
                        }
                    }
                    else
                    {
                        if (targetStat instanceof Achievement)
                        {
                            Achievement targetAchievement = (Achievement)targetStat;

                            if (isGiving)
                            {
                                if (targetPlayer.getStatFile().hasAchievementUnlocked(targetAchievement))
                                {
                                    throw new CommandException("commands.achievement.alreadyHave", new Object[] {targetPlayer.getName(), targetStat.createChatComponent()});
                                }

                                List<Achievement> missingParents;

                                for (missingParents = Lists.<Achievement>newArrayList(); targetAchievement.parentAchievement != null && !targetPlayer.getStatFile().hasAchievementUnlocked(targetAchievement.parentAchievement); targetAchievement = targetAchievement.parentAchievement)
                                {
                                    missingParents.add(targetAchievement.parentAchievement);
                                }

                                for (Achievement parentAchievement : Lists.reverse(missingParents))
                                {
                                    targetPlayer.triggerAchievement(parentAchievement);
                                }
                            }
                            else if (isTaking)
                            {
                                if (!targetPlayer.getStatFile().hasAchievementUnlocked(targetAchievement))
                                {
                                    throw new CommandException("commands.achievement.dontHave", new Object[] {targetPlayer.getName(), targetStat.createChatComponent()});
                                }

                                List<Achievement> unlockedAchievements = Lists.newArrayList(Iterators.filter(AchievementList.achievementList.iterator(), new Predicate<Achievement>()
                                {
                                    public boolean apply(Achievement achievement)
                                    {
                                        return targetPlayer.getStatFile().hasAchievementUnlocked(achievement) && achievement != targetStat;
                                    }
                                }));
                                List<Achievement> achievementsToRemove = Lists.newArrayList(unlockedAchievements);

                                for (Achievement unlockedAchievement : unlockedAchievements)
                                {
                                    Achievement dependencyCursor = unlockedAchievement;
                                    boolean dependsOnTarget;

                                    for (dependsOnTarget = false; dependencyCursor != null; dependencyCursor = dependencyCursor.parentAchievement)
                                    {
                                        if (dependencyCursor == targetStat)
                                        {
                                            dependsOnTarget = true;
                                        }
                                    }

                                    if (!dependsOnTarget)
                                    {
                                        for (dependencyCursor = unlockedAchievement; dependencyCursor != null; dependencyCursor = dependencyCursor.parentAchievement)
                                        {
                                            achievementsToRemove.remove(unlockedAchievement);
                                        }
                                    }
                                }

                                for (Achievement achievement : achievementsToRemove)
                                {
                                    targetPlayer.resetStat(achievement);
                                }
                            }
                        }

                        if (isGiving)
                        {
                            targetPlayer.triggerAchievement(targetStat);
                            notifyOperators(sender, this, "commands.achievement.give.success.one", new Object[] {targetPlayer.getName(), targetStat.createChatComponent()});
                        }
                        else if (isTaking)
                        {
                            targetPlayer.resetStat(targetStat);
                            notifyOperators(sender, this, "commands.achievement.take.success.one", new Object[] {targetStat.createChatComponent(), targetPlayer.getName()});
                        }
                    }
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, new String[] {"give", "take"});
        }
        else if (args.length != 2)
        {
            return args.length == 3 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
        }
        else
        {
            List<String> list = Lists.<String>newArrayList();

            for (StatBase statBase : StatList.allStats)
            {
                list.add(statBase.statId);
            }

            return getListOfStringsMatchingLastWord(args, list);
        }
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 2;
    }
}
