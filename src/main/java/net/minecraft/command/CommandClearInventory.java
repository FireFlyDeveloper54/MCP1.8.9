package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;

public class CommandClearInventory extends CommandBase
{
    public String getCommandName()
    {
        return "clear";
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.clear.usage";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayerMP targetPlayer = args.length == 0 ? getCommandSenderAsPlayer(sender) : getPlayer(sender, args[0]);
        Item item = args.length >= 2 ? getItemByText(sender, args[1]) : null;
        int itemMetadata = args.length >= 3 ? parseInt(args[2], -1) : -1;
        int maxCount = args.length >= 4 ? parseInt(args[3], -1) : -1;
        NBTTagCompound nbtFilter = null;

        if (args.length >= 5)
        {
            try
            {
                nbtFilter = JsonToNBT.getTagFromJson(buildString(args, 4));
            }
            catch (NBTException nbtException)
            {
                throw new CommandException("commands.clear.tagError", new Object[] {nbtException.getMessage()});
            }
        }

        if (args.length >= 2 && item == null)
        {
            throw new CommandException("commands.clear.failure", new Object[] {targetPlayer.getName()});
        }
        else
        {
            int removedCount = targetPlayer.inventory.clearMatchingItems(item, itemMetadata, maxCount, nbtFilter);
            targetPlayer.inventoryContainer.detectAndSendChanges();

            if (!targetPlayer.capabilities.isCreativeMode)
            {
                targetPlayer.updateHeldItem();
            }

            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, removedCount);

            if (removedCount == 0)
            {
                throw new CommandException("commands.clear.failure", new Object[] {targetPlayer.getName()});
            }
            else
            {
                if (maxCount == 0)
                {
                    sender.addChatMessage(new ChatComponentTranslation("commands.clear.testing", new Object[] {targetPlayer.getName(), Integer.valueOf(removedCount)}));
                }
                else
                {
                    notifyOperators(sender, this, "commands.clear.success", new Object[] {targetPlayer.getName(), Integer.valueOf(removedCount)});
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, this.getAllUsernames()) : (args.length == 2 ? getListOfStringsMatchingLastWord(args, Item.itemRegistry.getKeys()) : null);
    }

    protected String[] getAllUsernames()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
