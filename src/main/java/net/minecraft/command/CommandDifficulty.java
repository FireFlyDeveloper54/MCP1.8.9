package net.minecraft.command;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.EnumDifficulty;

public class CommandDifficulty extends CommandBase
{
    public String getCommandName()
    {
        return "difficulty";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.difficulty.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length <= 0)
        {
            throw new WrongUsageException("commands.difficulty.usage", new Object[0]);
        }
        else
        {
            EnumDifficulty enumdifficulty = this.getDifficultyFromCommand(args[0]);
            MinecraftServer.getServer().setDifficultyForAllWorlds(enumdifficulty);
            notifyOperators(sender, this, "commands.difficulty.success", new Object[] {new ChatComponentTranslation(enumdifficulty.getDifficultyResourceKey(), new Object[0])});
        }
    }

    protected EnumDifficulty getDifficultyFromCommand(String difficultyArgument) throws CommandException, NumberInvalidException
    {
        return !difficultyArgument.equalsIgnoreCase("peaceful") && !difficultyArgument.equalsIgnoreCase("p") ? (!difficultyArgument.equalsIgnoreCase("easy") && !difficultyArgument.equalsIgnoreCase("e") ? (!difficultyArgument.equalsIgnoreCase("normal") && !difficultyArgument.equalsIgnoreCase("n") ? (!difficultyArgument.equalsIgnoreCase("hard") && !difficultyArgument.equalsIgnoreCase("h") ? EnumDifficulty.getDifficultyEnum(parseInt(difficultyArgument, 0, 3)) : EnumDifficulty.HARD) : EnumDifficulty.NORMAL) : EnumDifficulty.EASY) : EnumDifficulty.PEACEFUL;
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"peaceful", "easy", "normal", "hard"}): null;
    }
}
