package net.minecraft.command;

import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class CommandCompare extends CommandBase
{
    public String getCommandName()
    {
        return "testforblocks";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.compare.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 9)
        {
            throw new WrongUsageException("commands.compare.usage", new Object[0]);
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

            if (volume > 524288)
            {
                throw new CommandException("commands.compare.tooManyBlocks", new Object[] {Integer.valueOf(volume), Integer.valueOf(524288)});
            }
            else if (sourceBox.minY >= 0 && sourceBox.maxY < 256 && destinationBox.minY >= 0 && destinationBox.maxY < 256)
            {
                World world = sender.getEntityWorld();

                if (world.isAreaLoaded(sourceBox) && world.isAreaLoaded(destinationBox))
                {
                    boolean maskedMode = false;

                    if (args.length > 9 && args[9].equals("masked"))
                    {
                        maskedMode = true;
                    }

                    int comparedBlocks = 0;
                    BlockPos destinationOffset = new BlockPos(destinationBox.minX - sourceBox.minX, destinationBox.minY - sourceBox.minY, destinationBox.minZ - sourceBox.minZ);
                    BlockPos.MutableBlockPos sourceCursor = new BlockPos.MutableBlockPos();
                    BlockPos.MutableBlockPos destinationCursor = new BlockPos.MutableBlockPos();

                    for (int sourceZ = sourceBox.minZ; sourceZ <= sourceBox.maxZ; ++sourceZ)
                    {
                        for (int sourceY = sourceBox.minY; sourceY <= sourceBox.maxY; ++sourceY)
                        {
                            for (int sourceX = sourceBox.minX; sourceX <= sourceBox.maxX; ++sourceX)
                            {
                                sourceCursor.set(sourceX, sourceY, sourceZ);
                                destinationCursor.set(sourceX + destinationOffset.getX(), sourceY + destinationOffset.getY(), sourceZ + destinationOffset.getZ());
                                boolean blocksMismatch = false;
                                IBlockState sourceState = world.getBlockState(sourceCursor);

                                if (!maskedMode || sourceState.getBlock() != Blocks.air)
                                {
                                    if (sourceState == world.getBlockState(destinationCursor))
                                    {
                                        TileEntity sourceTileEntity = world.getTileEntity(sourceCursor);
                                        TileEntity destinationTileEntity = world.getTileEntity(destinationCursor);

                                        if (sourceTileEntity != null && destinationTileEntity != null)
                                        {
                                            NBTTagCompound sourceTileData = new NBTTagCompound();
                                            sourceTileEntity.writeToNBT(sourceTileData);
                                            sourceTileData.removeTag("x");
                                            sourceTileData.removeTag("y");
                                            sourceTileData.removeTag("z");
                                            NBTTagCompound destinationTileData = new NBTTagCompound();
                                            destinationTileEntity.writeToNBT(destinationTileData);
                                            destinationTileData.removeTag("x");
                                            destinationTileData.removeTag("y");
                                            destinationTileData.removeTag("z");

                                            if (!sourceTileData.equals(destinationTileData))
                                            {
                                                blocksMismatch = true;
                                            }
                                        }
                                        else if (sourceTileEntity != null)
                                        {
                                            blocksMismatch = true;
                                        }
                                    }
                                    else
                                    {
                                        blocksMismatch = true;
                                    }

                                    ++comparedBlocks;

                                    if (blocksMismatch)
                                    {
                                        throw new CommandException("commands.compare.failed", new Object[0]);
                                    }
                                }
                            }
                        }
                    }

                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, comparedBlocks);
                    notifyOperators(sender, this, "commands.compare.success", new Object[] {Integer.valueOf(comparedBlocks)});
                }
                else
                {
                    throw new CommandException("commands.compare.outOfWorld", new Object[0]);
                }
            }
            else
            {
                throw new CommandException("commands.compare.outOfWorld", new Object[0]);
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length > 0 && args.length <= 3 ? getTabCompletionCoordinate(args, 0, pos) : (args.length > 3 && args.length <= 6 ? getTabCompletionCoordinate(args, 3, pos) : (args.length > 6 && args.length <= 9 ? getTabCompletionCoordinate(args, 6, pos) : (args.length == 10 ? getListOfStringsMatchingLastWord(args, new String[] {"masked", "all"}): null)));
    }
}
