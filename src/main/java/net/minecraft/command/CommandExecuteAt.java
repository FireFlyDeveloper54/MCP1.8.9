package net.minecraft.command;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CommandExecuteAt extends CommandBase
{
    public String getCommandName()
    {
        return "execute";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.execute.usage";
    }

    public void processCommand(final ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 5)
        {
            throw new WrongUsageException("commands.execute.usage", new Object[0]);
        }
        else
        {
            final Entity entity = getEntity(sender, args[0], Entity.class);
            final double executionX = parseDouble(entity.posX, args[1], false);
            final double executionY = parseDouble(entity.posY, args[2], false);
            final double executionZ = parseDouble(entity.posZ, args[3], false);
            final BlockPos executionPos = new BlockPos(executionX, executionY, executionZ);
            int commandStartIndex = 4;

            if ("detect".equals(args[4]) && args.length > 10)
            {
                World world = entity.getEntityWorld();
                double detectX = parseDouble(executionX, args[5], false);
                double detectY = parseDouble(executionY, args[6], false);
                double detectZ = parseDouble(executionZ, args[7], false);
                Block expectedBlock = getBlockByText(sender, args[8]);
                int expectedMetadata = parseInt(args[9], -1, 15);
                BlockPos detectPos = new BlockPos(detectX, detectY, detectZ);
                IBlockState detectedState = world.getBlockState(detectPos);

                if (detectedState.getBlock() != expectedBlock || expectedMetadata >= 0 && detectedState.getBlock().getMetaFromState(detectedState) != expectedMetadata)
                {
                    throw new CommandException("commands.execute.failed", new Object[] {"detect", entity.getName()});
                }

                commandStartIndex = 10;
            }

            String commandText = buildString(args, commandStartIndex);
            ICommandSender commandSender = new ICommandSender()
            {
                public String getName()
                {
                    return entity.getName();
                }
                public IChatComponent getDisplayName()
                {
                    return entity.getDisplayName();
                }
                public void addChatMessage(IChatComponent component)
                {
                    sender.addChatMessage(component);
                }
                public boolean canCommandSenderUseCommand(int permLevel, String commandName)
                {
                    return sender.canCommandSenderUseCommand(permLevel, commandName);
                }
                public BlockPos getPosition()
                {
                    return executionPos;
                }
                public Vec3 getPositionVector()
                {
                    return new Vec3(executionX, executionY, executionZ);
                }
                public World getEntityWorld()
                {
                    return entity.worldObj;
                }
                public Entity getCommandSenderEntity()
                {
                    return entity;
                }
                public boolean sendCommandFeedback()
                {
                    MinecraftServer minecraftServer = MinecraftServer.getServer();
                    return minecraftServer == null || minecraftServer.worldServers[0].getGameRules().getBoolean("commandBlockOutput");
                }
                public void setCommandStat(CommandResultStats.Type type, int amount)
                {
                    entity.setCommandStat(type, amount);
                }
            };
            ICommandManager commandManager = MinecraftServer.getServer().getCommandManager();

            try
            {
                int successfulInvocations = commandManager.executeCommand(commandSender, commandText);

                if (successfulInvocations < 1)
                {
                    throw new CommandException("commands.execute.allInvocationsFailed", new Object[] {commandText});
                }
            }
            catch (Throwable caughtThrowable)
            {
                throw new CommandException("commands.execute.failed", new Object[] {commandText, entity.getName()});
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : (args.length > 1 && args.length <= 4 ? getTabCompletionCoordinate(args, 1, pos) : (args.length > 5 && args.length <= 8 && "detect".equals(args[4]) ? getTabCompletionCoordinate(args, 5, pos) : (args.length == 9 && "detect".equals(args[4]) ? getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys()) : null)));
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
