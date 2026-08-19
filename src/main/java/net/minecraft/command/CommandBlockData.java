package net.minecraft.command;

import java.util.List;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CommandBlockData extends CommandBase
{
    public String getCommandName()
    {
        return "blockdata";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.blockdata.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 4)
        {
            throw new WrongUsageException("commands.blockdata.usage", new Object[0]);
        }
        else
        {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos targetPos = parseBlockPos(sender, args, 0, false);
            World world = sender.getEntityWorld();

            if (!world.isBlockLoaded(targetPos))
            {
                throw new CommandException("commands.blockdata.outOfWorld", new Object[0]);
            }
            else
            {
                TileEntity tileEntity = world.getTileEntity(targetPos);

                if (tileEntity == null)
                {
                    throw new CommandException("commands.blockdata.notValid", new Object[0]);
                }
                else
                {
                    NBTTagCompound mergedData = new NBTTagCompound();
                    tileEntity.writeToNBT(mergedData);
                    NBTTagCompound originalData = (NBTTagCompound)mergedData.copy();
                    NBTTagCompound inputData;

                    try
                    {
                        inputData = JsonToNBT.getTagFromJson(getChatComponentFromNthArg(sender, args, 3).getUnformattedText());
                    }
                    catch (NBTException nbtException)
                    {
                        throw new CommandException("commands.blockdata.tagError", new Object[] {nbtException.getMessage()});
                    }

                    mergedData.merge(inputData);
                    mergedData.setInteger("x", targetPos.getX());
                    mergedData.setInteger("y", targetPos.getY());
                    mergedData.setInteger("z", targetPos.getZ());

                    if (mergedData.equals(originalData))
                    {
                        throw new CommandException("commands.blockdata.failed", new Object[] {mergedData.toString()});
                    }
                    else
                    {
                        tileEntity.readFromNBT(mergedData);
                        tileEntity.markDirty();
                        world.markBlockForUpdate(targetPos);
                        sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
                        notifyOperators(sender, this, "commands.blockdata.success", new Object[] {mergedData.toString()});
                    }
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length > 0 && args.length <= 3 ? getTabCompletionCoordinate(args, 0, pos) : null;
    }
}
