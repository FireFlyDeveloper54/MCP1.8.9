package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandXP extends CommandBase
{
    public String getCommandName()
    {
        return "xp";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.xp.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length <= 0)
        {
            throw new WrongUsageException("commands.xp.usage", new Object[0]);
        }
        else
        {
            String amountText = args[0];
            boolean isLevelAmount = amountText.endsWith("l") || amountText.endsWith("L");

            if (isLevelAmount && amountText.length() > 1)
            {
                amountText = amountText.substring(0, amountText.length() - 1);
            }

            int amount = parseInt(amountText);
            boolean isNegative = amount < 0;

            if (isNegative)
            {
                amount *= -1;
            }

            EntityPlayer targetPlayer = args.length > 1 ? getPlayer(sender, args[1]) : getCommandSenderAsPlayer(sender);

            if (isLevelAmount)
            {
                sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, targetPlayer.experienceLevel);

                if (isNegative)
                {
                    targetPlayer.addExperienceLevel(-amount);
                    notifyOperators(sender, this, "commands.xp.success.negative.levels", new Object[] {Integer.valueOf(amount), targetPlayer.getName()});
                }
                else
                {
                    targetPlayer.addExperienceLevel(amount);
                    notifyOperators(sender, this, "commands.xp.success.levels", new Object[] {Integer.valueOf(amount), targetPlayer.getName()});
                }
            }
            else
            {
                sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, targetPlayer.experienceTotal);

                if (isNegative)
                {
                    throw new CommandException("commands.xp.failure.widthdrawXp", new Object[0]);
                }

                targetPlayer.addExperience(amount);
                notifyOperators(sender, this, "commands.xp.success", new Object[] {Integer.valueOf(amount), targetPlayer.getName()});
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, this.getAllUsernames()) : null;
    }

    protected String[] getAllUsernames()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 1;
    }
}
