package net.minecraft.command.server;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CommandTestForBlock extends CommandBase
{
    public String getCommandName()
    {
        return "testforblock";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.testforblock.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 4)
        {
            throw new WrongUsageException("commands.testforblock.usage", new Object[0]);
        }
        else
        {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos targetPos = parseBlockPos(sender, args, 0, false);
            Block expectedBlock = Block.getBlockFromName(args[3]);

            if (expectedBlock == null)
            {
                throw new NumberInvalidException("commands.setblock.notFound", new Object[] {args[3]});
            }
            else
            {
                int expectedMetadata = -1;

                if (args.length >= 5)
                {
                    expectedMetadata = parseInt(args[4], -1, 15);
                }

                World world = sender.getEntityWorld();

                if (!world.isBlockLoaded(targetPos))
                {
                    throw new CommandException("commands.testforblock.outOfWorld", new Object[0]);
                }
                else
                {
                    NBTTagCompound expectedTileData = new NBTTagCompound();
                    boolean hasExpectedTileData = false;

                    if (args.length >= 6 && expectedBlock.hasTileEntity())
                    {
                        String nbtText = getChatComponentFromNthArg(sender, args, 5).getUnformattedText();

                        try
                        {
                            expectedTileData = JsonToNBT.getTagFromJson(nbtText);
                            hasExpectedTileData = true;
                        }
                        catch (NBTException nbtException)
                        {
                            throw new CommandException("commands.setblock.tagError", new Object[] {nbtException.getMessage()});
                        }
                    }

                    IBlockState actualState = world.getBlockState(targetPos);
                    Block actualBlock = actualState.getBlock();

                    if (actualBlock != expectedBlock)
                    {
                        throw new CommandException("commands.testforblock.failed.tile", new Object[] {Integer.valueOf(targetPos.getX()), Integer.valueOf(targetPos.getY()), Integer.valueOf(targetPos.getZ()), actualBlock.getLocalizedName(), expectedBlock.getLocalizedName()});
                    }
                    else
                    {
                        if (expectedMetadata > -1)
                        {
                            int actualMetadata = actualState.getBlock().getMetaFromState(actualState);

                            if (actualMetadata != expectedMetadata)
                            {
                                throw new CommandException("commands.testforblock.failed.data", new Object[] {Integer.valueOf(targetPos.getX()), Integer.valueOf(targetPos.getY()), Integer.valueOf(targetPos.getZ()), Integer.valueOf(actualMetadata), Integer.valueOf(expectedMetadata)});
                            }
                        }

                        if (hasExpectedTileData)
                        {
                            TileEntity tileEntity = world.getTileEntity(targetPos);

                            if (tileEntity == null)
                            {
                                throw new CommandException("commands.testforblock.failed.tileEntity", new Object[] {Integer.valueOf(targetPos.getX()), Integer.valueOf(targetPos.getY()), Integer.valueOf(targetPos.getZ())});
                            }

                            NBTTagCompound actualTileData = new NBTTagCompound();
                            tileEntity.writeToNBT(actualTileData);

                            if (!NBTUtil.compareTags(expectedTileData, actualTileData, true))
                            {
                                throw new CommandException("commands.testforblock.failed.nbt", new Object[] {Integer.valueOf(targetPos.getX()), Integer.valueOf(targetPos.getY()), Integer.valueOf(targetPos.getZ())});
                            }
                        }

                        sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
                        notifyOperators(sender, this, "commands.testforblock.success", new Object[] {Integer.valueOf(targetPos.getX()), Integer.valueOf(targetPos.getY()), Integer.valueOf(targetPos.getZ())});
                    }
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length > 0 && args.length <= 3 ? getTabCompletionCoordinate(args, 0, pos) : (args.length == 4 ? getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys()) : null);
    }
}
