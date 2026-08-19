package net.minecraft.command;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

public class CommandEntityData extends CommandBase
{
    public String getCommandName()
    {
        return "entitydata";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.entitydata.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException("commands.entitydata.usage", new Object[0]);
        }
        else
        {
            Entity entity = getEntity(sender, args[0]);

            if (entity instanceof EntityPlayer)
            {
                throw new CommandException("commands.entitydata.noPlayers", new Object[] {entity.getDisplayName()});
            }
            else
            {
                NBTTagCompound mergedData = new NBTTagCompound();
                entity.writeToNBT(mergedData);
                NBTTagCompound originalData = (NBTTagCompound)mergedData.copy();
                NBTTagCompound inputData;

                try
                {
                    inputData = JsonToNBT.getTagFromJson(getChatComponentFromNthArg(sender, args, 1).getUnformattedText());
                }
                catch (NBTException nbtException)
                {
                    throw new CommandException("commands.entitydata.tagError", new Object[] {nbtException.getMessage()});
                }

                inputData.removeTag("UUIDMost");
                inputData.removeTag("UUIDLeast");
                mergedData.merge(inputData);

                if (mergedData.equals(originalData))
                {
                    throw new CommandException("commands.entitydata.failed", new Object[] {mergedData.toString()});
                }
                else
                {
                    entity.readFromNBT(mergedData);
                    notifyOperators(sender, this, "commands.entitydata.success", new Object[] {mergedData.toString()});
                }
            }
        }
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
