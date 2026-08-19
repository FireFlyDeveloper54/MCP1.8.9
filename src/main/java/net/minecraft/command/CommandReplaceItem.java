package net.minecraft.command;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CommandReplaceItem extends CommandBase
{
    private static final Map<String, Integer> SHORTCUTS = Maps.<String, Integer>newHashMap();

    public String getCommandName()
    {
        return "replaceitem";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.replaceitem.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.replaceitem.usage", new Object[0]);
        }
        else
        {
            boolean blockMode;

            if (args[0].equals("entity"))
            {
                blockMode = false;
            }
            else
            {
                if (!args[0].equals("block"))
                {
                    throw new WrongUsageException("commands.replaceitem.usage", new Object[0]);
                }

                blockMode = true;
            }

            int itemArgIndex;

            if (blockMode)
            {
                if (args.length < 6)
                {
                    throw new WrongUsageException("commands.replaceitem.block.usage", new Object[0]);
                }

                itemArgIndex = 4;
            }
            else
            {
                if (args.length < 4)
                {
                    throw new WrongUsageException("commands.replaceitem.entity.usage", new Object[0]);
                }

                itemArgIndex = 2;
            }

            int slotIndex = this.getSlotForShortcut(args[itemArgIndex++]);
            Item item;

            try
            {
                item = getItemByText(sender, args[itemArgIndex]);
            }
            catch (NumberInvalidException numberinvalidexception)
            {
                if (Block.getBlockFromName(args[itemArgIndex]) != Blocks.air)
                {
                    throw numberinvalidexception;
                }

                item = null;
            }

            ++itemArgIndex;
            int itemCount = args.length > itemArgIndex ? parseInt(args[itemArgIndex++], 1, 64) : 1;
            int itemMetadata = args.length > itemArgIndex ? parseInt(args[itemArgIndex++]) : 0;
            ItemStack stack = new ItemStack(item, itemCount, itemMetadata);

            if (args.length > itemArgIndex)
            {
                String nbtText = getChatComponentFromNthArg(sender, args, itemArgIndex).getUnformattedText();

                try
                {
                    stack.setTagCompound(JsonToNBT.getTagFromJson(nbtText));
                }
                catch (NBTException nbtException)
                {
                    throw new CommandException("commands.replaceitem.tagError", new Object[] {nbtException.getMessage()});
                }
            }

            if (stack.getItem() == null)
            {
                stack = null;
            }

            if (blockMode)
            {
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);
                BlockPos targetPos = parseBlockPos(sender, args, 1, false);
                World world = sender.getEntityWorld();
                TileEntity tileEntity = world.getTileEntity(targetPos);

                if (tileEntity == null || !(tileEntity instanceof IInventory))
                {
                    throw new CommandException("commands.replaceitem.noContainer", new Object[] {Integer.valueOf(targetPos.getX()), Integer.valueOf(targetPos.getY()), Integer.valueOf(targetPos.getZ())});
                }

                IInventory inventory = (IInventory)tileEntity;

                if (slotIndex >= 0 && slotIndex < inventory.getSizeInventory())
                {
                    inventory.setInventorySlotContents(slotIndex, stack);
                }
            }
            else
            {
                Entity entity = getEntity(sender, args[1]);
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);

                if (entity instanceof EntityPlayer)
                {
                    ((EntityPlayer)entity).inventoryContainer.detectAndSendChanges();
                }

                if (!entity.replaceItemInInventory(slotIndex, stack))
                {
                    throw new CommandException("commands.replaceitem.failed", new Object[] {Integer.valueOf(slotIndex), Integer.valueOf(itemCount), stack == null ? "Air" : stack.getChatComponent()});
                }

                if (entity instanceof EntityPlayer)
                {
                    ((EntityPlayer)entity).inventoryContainer.detectAndSendChanges();
                }
            }

            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, itemCount);
            notifyOperators(sender, this, "commands.replaceitem.success", new Object[] {Integer.valueOf(slotIndex), Integer.valueOf(itemCount), stack == null ? "Air" : stack.getChatComponent()});
        }
    }

    private int getSlotForShortcut(String shortcut) throws CommandException
    {
        Integer slot = SHORTCUTS.get(shortcut);

        if (slot == null)
        {
            throw new CommandException("commands.generic.parameter.invalid", new Object[] {shortcut});
        }
        else
        {
            return slot.intValue();
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"entity", "block"}): (args.length == 2 && args[0].equals("entity") ? getListOfStringsMatchingLastWord(args, this.getUsernames()) : (args.length >= 2 && args.length <= 4 && args[0].equals("block") ? getTabCompletionCoordinate(args, 1, pos) : ((args.length != 3 || !args[0].equals("entity")) && (args.length != 5 || !args[0].equals("block")) ? ((args.length != 4 || !args[0].equals("entity")) && (args.length != 6 || !args[0].equals("block")) ? null : getListOfStringsMatchingLastWord(args, Item.itemRegistry.getKeys())) : getListOfStringsMatchingLastWord(args, SHORTCUTS.keySet()))));
    }

    protected String[] getUsernames()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return args.length > 0 && args[0].equals("entity") && index == 1;
    }

    static
    {
        for (int containerSlot = 0; containerSlot < 54; ++containerSlot)
        {
            SHORTCUTS.put("slot.container." + containerSlot, Integer.valueOf(containerSlot));
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
        {
            SHORTCUTS.put("slot.hotbar." + hotbarSlot, Integer.valueOf(hotbarSlot));
        }

        for (int inventorySlot = 0; inventorySlot < 27; ++inventorySlot)
        {
            SHORTCUTS.put("slot.inventory." + inventorySlot, Integer.valueOf(9 + inventorySlot));
        }

        for (int enderChestSlot = 0; enderChestSlot < 27; ++enderChestSlot)
        {
            SHORTCUTS.put("slot.enderchest." + enderChestSlot, Integer.valueOf(200 + enderChestSlot));
        }

        for (int villagerSlot = 0; villagerSlot < 8; ++villagerSlot)
        {
            SHORTCUTS.put("slot.villager." + villagerSlot, Integer.valueOf(300 + villagerSlot));
        }

        for (int horseSlot = 0; horseSlot < 15; ++horseSlot)
        {
            SHORTCUTS.put("slot.horse." + horseSlot, Integer.valueOf(500 + horseSlot));
        }

        SHORTCUTS.put("slot.weapon", Integer.valueOf(99));
        SHORTCUTS.put("slot.armor.head", Integer.valueOf(103));
        SHORTCUTS.put("slot.armor.chest", Integer.valueOf(102));
        SHORTCUTS.put("slot.armor.legs", Integer.valueOf(101));
        SHORTCUTS.put("slot.armor.feet", Integer.valueOf(100));
        SHORTCUTS.put("slot.horse.saddle", Integer.valueOf(400));
        SHORTCUTS.put("slot.horse.armor", Integer.valueOf(401));
        SHORTCUTS.put("slot.horse.chest", Integer.valueOf(499));
    }
}
