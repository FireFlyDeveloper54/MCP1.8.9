package net.minecraft.command;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CommandFill extends CommandBase
{
    public String getCommandName()
    {
        return "fill";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.fill.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 7)
        {
            throw new WrongUsageException("commands.fill.usage", new Object[0]);
        }
        else
        {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos startPos = parseBlockPos(sender, args, 0, false);
            BlockPos endPos = parseBlockPos(sender, args, 3, false);
            Block targetBlock = CommandBase.getBlockByText(sender, args[6]);
            int targetMetadata = 0;

            if (args.length >= 8)
            {
                targetMetadata = parseInt(args[7], 0, 15);
            }

            BlockPos minPos = new BlockPos(Math.min(startPos.getX(), endPos.getX()), Math.min(startPos.getY(), endPos.getY()), Math.min(startPos.getZ(), endPos.getZ()));
            BlockPos maxPos = new BlockPos(Math.max(startPos.getX(), endPos.getX()), Math.max(startPos.getY(), endPos.getY()), Math.max(startPos.getZ(), endPos.getZ()));
            int volume = (maxPos.getX() - minPos.getX() + 1) * (maxPos.getY() - minPos.getY() + 1) * (maxPos.getZ() - minPos.getZ() + 1);

            if (volume > 32768)
            {
                throw new CommandException("commands.fill.tooManyBlocks", new Object[] {Integer.valueOf(volume), Integer.valueOf(32768)});
            }
            else if (minPos.getY() >= 0 && maxPos.getY() < 256)
            {
                World world = sender.getEntityWorld();

                for (int chunkCheckZ = minPos.getZ(); chunkCheckZ < maxPos.getZ() + 16; chunkCheckZ += 16)
                {
                    for (int chunkCheckX = minPos.getX(); chunkCheckX < maxPos.getX() + 16; chunkCheckX += 16)
                    {
                        if (!world.isBlockLoaded(new BlockPos(chunkCheckX, maxPos.getY() - minPos.getY(), chunkCheckZ)))
                        {
                            throw new CommandException("commands.fill.outOfWorld", new Object[0]);
                        }
                    }
                }

                NBTTagCompound tileEntityData = new NBTTagCompound();
                boolean hasTileEntityData = false;

                if (args.length >= 10 && targetBlock.hasTileEntity())
                {
                    String nbtText = getChatComponentFromNthArg(sender, args, 9).getUnformattedText();

                    try
                    {
                        tileEntityData = JsonToNBT.getTagFromJson(nbtText);
                        hasTileEntityData = true;
                    }
                    catch (NBTException nbtException)
                    {
                        throw new CommandException("commands.fill.tagError", new Object[] {nbtException.getMessage()});
                    }
                }

                List<BlockPos> changedPositions = Lists.<BlockPos>newArrayList();
                int affectedBlocks = 0;

                for (int fillZ = minPos.getZ(); fillZ <= maxPos.getZ(); ++fillZ)
                {
                    for (int fillY = minPos.getY(); fillY <= maxPos.getY(); ++fillY)
                    {
                        for (int fillX = minPos.getX(); fillX <= maxPos.getX(); ++fillX)
                        {
                            BlockPos currentPos = new BlockPos(fillX, fillY, fillZ);

                            if (args.length >= 9)
                            {
                                if (!args[8].equals("outline") && !args[8].equals("hollow"))
                                {
                                    if (args[8].equals("destroy"))
                                    {
                                        world.destroyBlock(currentPos, true);
                                    }
                                    else if (args[8].equals("keep"))
                                    {
                                        if (!world.isAirBlock(currentPos))
                                        {
                                            continue;
                                        }
                                    }
                                    else if (args[8].equals("replace") && !targetBlock.hasTileEntity())
                                    {
                                        if (args.length > 9)
                                        {
                                            Block replaceBlock = CommandBase.getBlockByText(sender, args[9]);

                                            if (world.getBlockState(currentPos).getBlock() != replaceBlock)
                                            {
                                                continue;
                                            }
                                        }

                                        if (args.length > 10)
                                        {
                                            int replaceMetadata = CommandBase.parseInt(args[10]);
                                            IBlockState existingState = world.getBlockState(currentPos);

                                            if (existingState.getBlock().getMetaFromState(existingState) != replaceMetadata)
                                            {
                                                continue;
                                            }
                                        }
                                    }
                                }
                                else if (fillX != minPos.getX() && fillX != maxPos.getX() && fillY != minPos.getY() && fillY != maxPos.getY() && fillZ != minPos.getZ() && fillZ != maxPos.getZ())
                                {
                                    if (args[8].equals("hollow"))
                                    {
                                        world.setBlockState(currentPos, Blocks.air.getDefaultState(), 2);
                                        changedPositions.add(currentPos);
                                    }

                                    continue;
                                }
                            }

                            TileEntity existingTileEntity = world.getTileEntity(currentPos);

                            if (existingTileEntity != null)
                            {
                                if (existingTileEntity instanceof IInventory)
                                {
                                    ((IInventory)existingTileEntity).clear();
                                }

                                world.setBlockState(currentPos, Blocks.barrier.getDefaultState(), targetBlock == Blocks.barrier ? 2 : 4);
                            }

                            IBlockState targetState = targetBlock.getStateFromMeta(targetMetadata);

                            if (world.setBlockState(currentPos, targetState, 2))
                            {
                                changedPositions.add(currentPos);
                                ++affectedBlocks;

                                if (hasTileEntityData)
                                {
                                    TileEntity newTileEntity = world.getTileEntity(currentPos);

                                    if (newTileEntity != null)
                                    {
                                        tileEntityData.setInteger("x", currentPos.getX());
                                        tileEntityData.setInteger("y", currentPos.getY());
                                        tileEntityData.setInteger("z", currentPos.getZ());
                                        newTileEntity.readFromNBT(tileEntityData);
                                    }
                                }
                            }
                        }
                    }
                }

                for (BlockPos changedPos : changedPositions)
                {
                    Block changedBlock = world.getBlockState(changedPos).getBlock();
                    world.notifyNeighborsRespectDebug(changedPos, changedBlock);
                }

                if (affectedBlocks <= 0)
                {
                    throw new CommandException("commands.fill.failed", new Object[0]);
                }
                else
                {
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, affectedBlocks);
                    notifyOperators(sender, this, "commands.fill.success", new Object[] {Integer.valueOf(affectedBlocks)});
                }
            }
            else
            {
                throw new CommandException("commands.fill.outOfWorld", new Object[0]);
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length > 0 && args.length <= 3 ? getTabCompletionCoordinate(args, 0, pos) : (args.length > 3 && args.length <= 6 ? getTabCompletionCoordinate(args, 3, pos) : (args.length == 7 ? getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys()) : (args.length == 9 ? getListOfStringsMatchingLastWord(args, new String[] {"replace", "destroy", "keep", "hollow", "outline"}): (args.length == 10 && "replace".equals(args[8]) ? getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys()) : null))));
    }
}
