package net.minecraft.client.settings;

import java.util.ArrayList;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class HotbarSnapshot extends ArrayList<ItemStack>
{
    public static final int HOTBAR_SIZE = InventoryPlayer.getHotbarSize();

    public HotbarSnapshot()
    {
        this.ensureCapacity(HOTBAR_SIZE);

        for (int slot = 0; slot < HOTBAR_SIZE; ++slot)
        {
            this.add((ItemStack)null);
        }
    }

    public NBTTagList createTag()
    {
        NBTTagList tagList = new NBTTagList();

        for (int slot = 0; slot < HOTBAR_SIZE; ++slot)
        {
            ItemStack itemStack = this.get(slot);
            tagList.appendTag(itemStack == null ? new NBTTagCompound() : itemStack.writeToNBT(new NBTTagCompound()));
        }

        return tagList;
    }

    public void fromTag(NBTTagList tagList)
    {
        for (int slot = 0; slot < HOTBAR_SIZE; ++slot)
        {
            this.set(slot, ItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(slot)));
        }
    }

    public boolean isEmpty()
    {
        for (int slot = 0; slot < HOTBAR_SIZE; ++slot)
        {
            if (this.get(slot) != null)
            {
                return false;
            }
        }

        return true;
    }
}
