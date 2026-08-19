package net.minecraft.command;

import com.google.common.collect.Lists;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class CommandClone extends CommandBase
{
    public String getCommandName()
    {
        return "clone";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.clone.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 9)
        {
            throw new WrongUsageException("commands.clone.usage", new Object[0]);
        }
        else
        {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos sourceStart = parseBlockPos(sender, args, 0, false);
            BlockPos sourceEnd = parseBlockPos(sender, args, 3, false);
            BlockPos destinationStart = parseBlockPos(sender, args, 6, false);
            StructureBoundingBox sourceBox = new StructureBoundingBox(sourceStart, sourceEnd);
            StructureBoundingBox destinationBox = new StructureBoundingBox(destinationStart, destinationStart.add(sourceBox.getLength()));
            int volume = sourceBox.getXSize() * sourceBox.getYSize() * sourceBox.getZSize();

            if (volume > 32768)
            {
                throw new CommandException("commands.clone.tooManyBlocks", new Object[] {Integer.valueOf(volume), Integer.valueOf(32768)});
            }
            else
            {
                boolean moveMode = false;
                Block filterBlock = null;
                int filterMetadata = -1;

                if ((args.length < 11 || !args[10].equals("force") && !args[10].equals("move")) && sourceBox.intersectsWith(destinationBox))
                {
                    throw new CommandException("commands.clone.noOverlap", new Object[0]);
                }
                else
                {
                    if (args.length >= 11 && args[10].equals("move"))
                    {
                        moveMode = true;
                    }

                    if (sourceBox.minY >= 0 && sourceBox.maxY < 256 && destinationBox.minY >= 0 && destinationBox.maxY < 256)
                    {
                        World world = sender.getEntityWorld();

                        if (world.isAreaLoaded(sourceBox) && world.isAreaLoaded(destinationBox))
                        {
                            boolean maskedMode = false;

                            if (args.length >= 10)
                            {
                                if (args[9].equals("masked"))
                                {
                                    maskedMode = true;
                                }
                                else if (args[9].equals("filtered"))
                                {
                                    if (args.length < 12)
                                    {
                                        throw new WrongUsageException("commands.clone.usage", new Object[0]);
                                    }

                                    filterBlock = getBlockByText(sender, args[11]);

                                    if (args.length >= 13)
                                    {
                                        filterMetadata = parseInt(args[12], 0, 15);
                                    }
                                }
                            }

                            List<CommandClone.StaticCloneData> fullBlockData = Lists.<CommandClone.StaticCloneData>newArrayList();
                            List<CommandClone.StaticCloneData> tileEntityData = Lists.<CommandClone.StaticCloneData>newArrayList();
                            List<CommandClone.StaticCloneData> nonFullBlockData = Lists.<CommandClone.StaticCloneData>newArrayList();
                            LinkedList<BlockPos> sourcePositions = Lists.<BlockPos>newLinkedList();
                            BlockPos destinationOffset = new BlockPos(destinationBox.minX - sourceBox.minX, destinationBox.minY - sourceBox.minY, destinationBox.minZ - sourceBox.minZ);

                            for (int sourceZ = sourceBox.minZ; sourceZ <= sourceBox.maxZ; ++sourceZ)
                            {
                                for (int sourceY = sourceBox.minY; sourceY <= sourceBox.maxY; ++sourceY)
                                {
                                    for (int sourceX = sourceBox.minX; sourceX <= sourceBox.maxX; ++sourceX)
                                    {
                                        BlockPos sourcePos = new BlockPos(sourceX, sourceY, sourceZ);
                                        BlockPos destinationPos = sourcePos.add(destinationOffset);
                                        IBlockState sourceState = world.getBlockState(sourcePos);

                                        if ((!maskedMode || sourceState.getBlock() != Blocks.air) && (filterBlock == null || sourceState.getBlock() == filterBlock && (filterMetadata < 0 || sourceState.getBlock().getMetaFromState(sourceState) == filterMetadata)))
                                        {
                                            TileEntity sourceTileEntity = world.getTileEntity(sourcePos);

                                            if (sourceTileEntity != null)
                                            {
                                                NBTTagCompound sourceTileData = new NBTTagCompound();
                                                sourceTileEntity.writeToNBT(sourceTileData);
                                                tileEntityData.add(new CommandClone.StaticCloneData(destinationPos, sourceState, sourceTileData));
                                                sourcePositions.addLast(sourcePos);
                                            }
                                            else if (!sourceState.getBlock().isFullBlock() && !sourceState.getBlock().isFullCube())
                                            {
                                                nonFullBlockData.add(new CommandClone.StaticCloneData(destinationPos, sourceState, (NBTTagCompound)null));
                                                sourcePositions.addFirst(sourcePos);
                                            }
                                            else
                                            {
                                                fullBlockData.add(new CommandClone.StaticCloneData(destinationPos, sourceState, (NBTTagCompound)null));
                                                sourcePositions.addLast(sourcePos);
                                            }
                                        }
                                    }
                                }
                            }

                            if (moveMode)
                            {
                                for (BlockPos sourcePos : sourcePositions)
                                {
                                    TileEntity sourceTileEntity = world.getTileEntity(sourcePos);

                                    if (sourceTileEntity instanceof IInventory)
                                    {
                                        ((IInventory)sourceTileEntity).clear();
                                    }

                                    world.setBlockState(sourcePos, Blocks.barrier.getDefaultState(), 2);
                                }

                                for (BlockPos sourcePos : sourcePositions)
                                {
                                    world.setBlockState(sourcePos, Blocks.air.getDefaultState(), 3);
                                }
                            }

                            List<CommandClone.StaticCloneData> orderedCloneData = Lists.<CommandClone.StaticCloneData>newArrayList();
                            orderedCloneData.addAll(fullBlockData);
                            orderedCloneData.addAll(tileEntityData);
                            orderedCloneData.addAll(nonFullBlockData);
                            List<CommandClone.StaticCloneData> reverseCloneData = Lists.<CommandClone.StaticCloneData>reverse(orderedCloneData);

                            for (CommandClone.StaticCloneData cloneData : reverseCloneData)
                            {
                                TileEntity existingTileEntity = world.getTileEntity(cloneData.pos);

                                if (existingTileEntity instanceof IInventory)
                                {
                                    ((IInventory)existingTileEntity).clear();
                                }

                                world.setBlockState(cloneData.pos, Blocks.barrier.getDefaultState(), 2);
                            }

                            int placedBlocks = 0;

                            for (CommandClone.StaticCloneData cloneData : orderedCloneData)
                            {
                                if (world.setBlockState(cloneData.pos, cloneData.blockState, 2))
                                {
                                    ++placedBlocks;
                                }
                            }

                            for (CommandClone.StaticCloneData cloneData : tileEntityData)
                            {
                                TileEntity destinationTileEntity = world.getTileEntity(cloneData.pos);

                                if (cloneData.compound != null && destinationTileEntity != null)
                                {
                                    cloneData.compound.setInteger("x", cloneData.pos.getX());
                                    cloneData.compound.setInteger("y", cloneData.pos.getY());
                                    cloneData.compound.setInteger("z", cloneData.pos.getZ());
                                    destinationTileEntity.readFromNBT(cloneData.compound);
                                    destinationTileEntity.markDirty();
                                }

                                world.setBlockState(cloneData.pos, cloneData.blockState, 2);
                            }

                            for (CommandClone.StaticCloneData cloneData : reverseCloneData)
                            {
                                world.notifyNeighborsRespectDebug(cloneData.pos, cloneData.blockState.getBlock());
                            }

                            List<NextTickListEntry> pendingUpdates = world.getPendingBlockUpdatesInArea(sourceBox, false);

                            if (pendingUpdates != null)
                            {
                                for (NextTickListEntry pendingUpdate : pendingUpdates)
                                {
                                    if (sourceBox.isVecInside(pendingUpdate.position))
                                    {
                                        BlockPos scheduledPos = pendingUpdate.position.add(destinationOffset);
                                        world.scheduleBlockUpdate(scheduledPos, pendingUpdate.getBlock(), (int)(pendingUpdate.scheduledTime - world.getWorldInfo().getWorldTotalTime()), pendingUpdate.priority);
                                    }
                                }
                            }

                            if (placedBlocks <= 0)
                            {
                                throw new CommandException("commands.clone.failed", new Object[0]);
                            }
                            else
                            {
                                sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, placedBlocks);
                                notifyOperators(sender, this, "commands.clone.success", new Object[] {Integer.valueOf(placedBlocks)});
                            }
                        }
                        else
                        {
                            throw new CommandException("commands.clone.outOfWorld", new Object[0]);
                        }
                    }
                    else
                    {
                        throw new CommandException("commands.clone.outOfWorld", new Object[0]);
                    }
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length > 0 && args.length <= 3 ? getTabCompletionCoordinate(args, 0, pos) : (args.length > 3 && args.length <= 6 ? getTabCompletionCoordinate(args, 3, pos) : (args.length > 6 && args.length <= 9 ? getTabCompletionCoordinate(args, 6, pos) : (args.length == 10 ? getListOfStringsMatchingLastWord(args, new String[] {"replace", "masked", "filtered"}): (args.length == 11 ? getListOfStringsMatchingLastWord(args, new String[] {"normal", "force", "move"}): (args.length == 12 && "filtered".equals(args[9]) ? getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys()) : null)))));
    }

    static class StaticCloneData
    {
        public final BlockPos pos;
        public final IBlockState blockState;
        public final NBTTagCompound compound;

        public StaticCloneData(BlockPos posIn, IBlockState stateIn, NBTTagCompound compoundIn)
        {
            this.pos = posIn;
            this.blockState = stateIn;
            this.compound = compoundIn;
        }
    }
}
