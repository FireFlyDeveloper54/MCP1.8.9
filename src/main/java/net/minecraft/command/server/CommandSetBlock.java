package net.minecraft.command.server;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CommandSetBlock extends CommandBase
{
    public String getCommandName()
    {
        return "setblock";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.setblock.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 4)
        {
            throw new WrongUsageException("commands.setblock.usage", new Object[0]);
        }
        else
        {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos targetPos = parseBlockPos(sender, args, 0, false);
            Block targetBlock = CommandBase.getBlockByText(sender, args[3]);
            int targetMetadata = 0;

            if (args.length >= 5)
            {
                targetMetadata = parseInt(args[4], 0, 15);
            }

            World world = sender.getEntityWorld();

            if (!world.isBlockLoaded(targetPos))
            {
                throw new CommandException("commands.setblock.outOfWorld", new Object[0]);
            }
            else
            {
                NBTTagCompound tileEntityData = new NBTTagCompound();
                boolean hasTileEntityData = false;

                if (args.length >= 7 && targetBlock.hasTileEntity())
                {
                    String nbtText = getChatComponentFromNthArg(sender, args, 6).getUnformattedText();

                    try
                    {
                        tileEntityData = JsonToNBT.getTagFromJson(nbtText);
                        hasTileEntityData = true;
                    }
                    catch (NBTException nbtException)
                    {
                        throw new CommandException("commands.setblock.tagError", new Object[] {nbtException.getMessage()});
                    }
                }

                if (args.length >= 6)
                {
                    if (args[5].equals("destroy"))
                    {
                        world.destroyBlock(targetPos, true);

                        if (targetBlock == Blocks.air)
                        {
                            notifyOperators(sender, this, "commands.setblock.success", new Object[0]);
                            return;
                        }
                    }
                    else if (args[5].equals("keep") && !world.isAirBlock(targetPos))
                    {
                        throw new CommandException("commands.setblock.noChange", new Object[0]);
                    }
                }

                TileEntity existingTileEntity = world.getTileEntity(targetPos);

                if (existingTileEntity != null)
                {
                    if (existingTileEntity instanceof IInventory)
                    {
                        ((IInventory)existingTileEntity).clear();
                    }

                    world.setBlockState(targetPos, Blocks.air.getDefaultState(), targetBlock == Blocks.air ? 2 : 4);
                }

                IBlockState targetState = targetBlock.getStateFromMeta(targetMetadata);

                if (!world.setBlockState(targetPos, targetState, 2))
                {
                    throw new CommandException("commands.setblock.noChange", new Object[0]);
                }
                else
                {
                    if (hasTileEntityData)
                    {
                        TileEntity newTileEntity = world.getTileEntity(targetPos);

                        if (newTileEntity != null)
                        {
                            tileEntityData.setInteger("x", targetPos.getX());
                            tileEntityData.setInteger("y", targetPos.getY());
                            tileEntityData.setInteger("z", targetPos.getZ());
                            newTileEntity.readFromNBT(tileEntityData);
                        }
                    }

                    world.notifyNeighborsRespectDebug(targetPos, targetState.getBlock());
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
                    notifyOperators(sender, this, "commands.setblock.success", new Object[0]);
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length > 0 && args.length <= 3 ? getTabCompletionCoordinate(args, 0, pos) : (args.length == 4 ? getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys()) : (args.length == 6 ? getListOfStringsMatchingLastWord(args, new String[] {"replace", "destroy", "keep"}): null));
    }
}
