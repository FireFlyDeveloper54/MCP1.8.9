package net.minecraft.command.server;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;

public class CommandBanIp extends CommandBase
{
    public static final Pattern IPV4_ADDRESS_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public String getCommandName()
    {
        return "ban-ip";
    }

    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return MinecraftServer.getServer().getConfigurationManager().getBannedIPs().isLanServer() && super.canCommandSenderUseCommand(sender);
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.banip.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length >= 1 && args[0].length() > 1)
        {
            IChatComponent reasonComponent = args.length >= 2 ? getChatComponentFromNthArg(sender, args, 1) : null;
            Matcher matcher = IPV4_ADDRESS_PATTERN.matcher(args[0]);

            if (matcher.matches())
            {
                this.banIpAddress(sender, args[0], reasonComponent == null ? null : reasonComponent.getUnformattedText());
            }
            else
            {
                EntityPlayerMP targetPlayer = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(args[0]);

                if (targetPlayer == null)
                {
                    throw new PlayerNotFoundException("commands.banip.invalid", new Object[0]);
                }

                this.banIpAddress(sender, targetPlayer.getPlayerIP(), reasonComponent == null ? null : reasonComponent.getUnformattedText());
            }
        }
        else
        {
            throw new WrongUsageException("commands.banip.usage", new Object[0]);
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }

    protected void banIpAddress(ICommandSender sender, String address, String reason)
    {
        IPBanEntry banEntry = new IPBanEntry(address, (Date)null, sender.getName(), (Date)null, reason);
        MinecraftServer.getServer().getConfigurationManager().getBannedIPs().addEntry(banEntry);
        List<EntityPlayerMP> matchingPlayers = MinecraftServer.getServer().getConfigurationManager().getPlayersMatchingAddress(address);
        String[] kickedPlayerNames = new String[matchingPlayers.size()];
        int playerIndex = 0;

        for (EntityPlayerMP player : matchingPlayers)
        {
            player.playerNetServerHandler.kickPlayerFromServer("You have been IP banned.");
            kickedPlayerNames[playerIndex++] = player.getName();
        }

        if (matchingPlayers.isEmpty())
        {
            notifyOperators(sender, this, "commands.banip.success", new Object[] {address});
        }
        else
        {
            notifyOperators(sender, this, "commands.banip.success.players", new Object[] {address, joinNiceString(kickedPlayerNames)});
        }
    }
}
