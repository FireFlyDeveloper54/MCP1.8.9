package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;

public class CommandGameMode extends CommandBase
{
    public String getCommandName()
    {
        return "gamemode";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.gamemode.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length <= 0)
        {
            throw new WrongUsageException("commands.gamemode.usage", new Object[0]);
        }
        else
        {
            WorldSettings.GameType targetGameType = this.getGameModeFromCommand(sender, args[0]);
            EntityPlayer targetPlayer = args.length >= 2 ? getPlayer(sender, args[1]) : getCommandSenderAsPlayer(sender);
            targetPlayer.setGameType(targetGameType);
            targetPlayer.fallDistance = 0.0F;

            if (sender.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback"))
            {
                targetPlayer.addChatMessage(new ChatComponentTranslation("gameMode.changed", new Object[0]));
            }

            IChatComponent gameModeName = new ChatComponentTranslation("gameMode." + targetGameType.getName(), new Object[0]);

            if (targetPlayer != sender)
            {
                notifyOperators(sender, this, 1, "commands.gamemode.success.other", new Object[] {targetPlayer.getName(), gameModeName});
            }
            else
            {
                notifyOperators(sender, this, 1, "commands.gamemode.success.self", new Object[] {gameModeName});
            }
        }
    }

    protected WorldSettings.GameType getGameModeFromCommand(ICommandSender sender, String gameModeArgument) throws CommandException, NumberInvalidException
    {
        return !gameModeArgument.equalsIgnoreCase(WorldSettings.GameType.SURVIVAL.getName()) && !gameModeArgument.equalsIgnoreCase("s") ? (!gameModeArgument.equalsIgnoreCase(WorldSettings.GameType.CREATIVE.getName()) && !gameModeArgument.equalsIgnoreCase("c") ? (!gameModeArgument.equalsIgnoreCase(WorldSettings.GameType.ADVENTURE.getName()) && !gameModeArgument.equalsIgnoreCase("a") ? (!gameModeArgument.equalsIgnoreCase(WorldSettings.GameType.SPECTATOR.getName()) && !gameModeArgument.equalsIgnoreCase("sp") ? WorldSettings.getGameTypeById(parseInt(gameModeArgument, 0, WorldSettings.GameType.VALUES.length - 2)) : WorldSettings.GameType.SPECTATOR) : WorldSettings.GameType.ADVENTURE) : WorldSettings.GameType.CREATIVE) : WorldSettings.GameType.SURVIVAL;
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"survival", "creative", "adventure", "spectator"}): (args.length == 2 ? getListOfStringsMatchingLastWord(args, this.getListOfPlayerUsernames()) : null);
    }

    protected String[] getListOfPlayerUsernames()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 1;
    }
}
