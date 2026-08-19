package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandServerKick extends CommandBase
{
    public String getCommandName()
    {
        return "kick";
    }

    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.kick.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 0 && args[0].length() > 1)
        {
            EntityPlayerMP targetPlayer = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(args[0]);
            String reason = "Kicked by an operator.";
            boolean hasCustomReason = false;

            if (targetPlayer == null)
            {
                throw new PlayerNotFoundException();
            }
            else
            {
                if (args.length >= 2)
                {
                    reason = getChatComponentFromNthArg(sender, args, 1).getUnformattedText();
                    hasCustomReason = true;
                }

                targetPlayer.playerNetServerHandler.kickPlayerFromServer(reason);

                if (hasCustomReason)
                {
                    notifyOperators(sender, this, "commands.kick.success.reason", new Object[] {targetPlayer.getName(), reason});
                }
                else
                {
                    notifyOperators(sender, this, "commands.kick.success", new Object[] {targetPlayer.getName()});
                }
            }
        }
        else
        {
            throw new WrongUsageException("commands.kick.usage", new Object[0]);
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }
}
