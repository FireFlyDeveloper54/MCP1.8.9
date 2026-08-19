package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandGive extends CommandBase
{
    public String getCommandName()
    {
        return "give";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.give.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException("commands.give.usage", new Object[0]);
        }
        else
        {
            EntityPlayer targetPlayer = getPlayer(sender, args[0]);
            Item item = getItemByText(sender, args[1]);
            int itemCount = args.length >= 3 ? parseInt(args[2], 1, 64) : 1;
            int itemMetadata = args.length >= 4 ? parseInt(args[3]) : 0;
            ItemStack stack = new ItemStack(item, itemCount, itemMetadata);

            if (args.length >= 5)
            {
                String nbtText = getChatComponentFromNthArg(sender, args, 4).getUnformattedText();

                try
                {
                    stack.setTagCompound(JsonToNBT.getTagFromJson(nbtText));
                }
                catch (NBTException nbtException)
                {
                    throw new CommandException("commands.give.tagError", new Object[] {nbtException.getMessage()});
                }
            }

            boolean addedToInventory = targetPlayer.inventory.addItemStackToInventory(stack);

            if (addedToInventory)
            {
                targetPlayer.worldObj.playSoundAtEntity(targetPlayer, "random.pop", 0.2F, ((targetPlayer.getRNG().nextFloat() - targetPlayer.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                targetPlayer.inventoryContainer.detectAndSendChanges();
            }

            if (addedToInventory && stack.stackSize <= 0)
            {
                stack.stackSize = 1;
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, itemCount);
                EntityItem fakeDrop = targetPlayer.dropPlayerItemWithRandomChoice(stack, false);

                if (fakeDrop != null)
                {
                    fakeDrop.makeFakeItem();
                }
            }
            else
            {
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, itemCount - stack.stackSize);
                EntityItem droppedRemainder = targetPlayer.dropPlayerItemWithRandomChoice(stack, false);

                if (droppedRemainder != null)
                {
                    droppedRemainder.setNoPickupDelay();
                    droppedRemainder.setOwner(targetPlayer.getName());
                }
            }

            notifyOperators(sender, this, "commands.give.success", new Object[] {stack.getChatComponent(), Integer.valueOf(itemCount), targetPlayer.getName()});
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, this.getPlayers()) : (args.length == 2 ? getListOfStringsMatchingLastWord(args, Item.itemRegistry.getKeys()) : null);
    }

    protected String[] getPlayers()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
