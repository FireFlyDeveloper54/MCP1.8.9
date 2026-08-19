package net.minecraft.command.server;

import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.BlockPos;

public class CommandBanPlayer extends CommandBase
{
    public String getCommandName()
    {
        return "ban";
    }

    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.ban.usage";
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().isLanServer() && super.canCommandSenderUseCommand(sender);
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length >= 1 && args[0].length() > 0)
        {
            MinecraftServer server = MinecraftServer.getServer();
            GameProfile targetProfile = server.getPlayerProfileCache().getGameProfileForUsername(args[0]);

            if (targetProfile == null)
            {
                throw new CommandException("commands.ban.failed", new Object[] {args[0]});
            }
            else
            {
                String reason = null;

                if (args.length >= 2)
                {
                    reason = getChatComponentFromNthArg(sender, args, 1).getUnformattedText();
                }

                UserListBansEntry banEntry = new UserListBansEntry(targetProfile, (Date)null, sender.getName(), (Date)null, reason);
                server.getConfigurationManager().getBannedPlayers().addEntry(banEntry);
                EntityPlayerMP targetPlayer = server.getConfigurationManager().getPlayerByUsername(args[0]);

                if (targetPlayer != null)
                {
                    targetPlayer.playerNetServerHandler.kickPlayerFromServer("You are banned from this server.");
                }

                notifyOperators(sender, this, "commands.ban.success", new Object[] {args[0]});
            }
        }
        else
        {
            throw new WrongUsageException("commands.ban.usage", new Object[0]);
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }
}
