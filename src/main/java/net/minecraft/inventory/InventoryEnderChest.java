package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityEnderChest;

public class InventoryEnderChest extends InventoryBasic
{
    private TileEntityEnderChest associatedChest;

    public InventoryEnderChest()
    {
        super("container.enderchest", false, 27);
    }

    public void setChestTileEntity(TileEntityEnderChest chestTileEntity)
    {
        this.associatedChest = chestTileEntity;
    }

    public void loadInventoryFromNBT(NBTTagList tagList)
    {
        for (int slotIndex = 0; slotIndex < this.getSizeInventory(); ++slotIndex)
        {
            this.setInventorySlotContents(slotIndex, (ItemStack)null);
        }

        for (int tagIndex = 0; tagIndex < tagList.tagCount(); ++tagIndex)
        {
            NBTTagCompound itemNbt = tagList.getCompoundTagAt(tagIndex);
            int slotIndex = itemNbt.getByte("Slot") & 255;

            if (slotIndex >= 0 && slotIndex < this.getSizeInventory())
            {
                this.setInventorySlotContents(slotIndex, ItemStack.loadItemStackFromNBT(itemNbt));
            }
        }
    }

    public NBTTagList saveInventoryToNBT()
    {
        NBTTagList nBTTagList = new NBTTagList();

        for (int slotIndex = 0; slotIndex < this.getSizeInventory(); ++slotIndex)
        {
            ItemStack itemStack = this.getStackInSlot(slotIndex);

            if (itemStack != null)
            {
                NBTTagCompound nBTTagCompound = new NBTTagCompound();
                nBTTagCompound.setByte("Slot", (byte)slotIndex);
                itemStack.writeToNBT(nBTTagCompound);
                nBTTagList.appendTag(nBTTagCompound);
            }
        }

        return nBTTagList;
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return this.associatedChest != null && !this.associatedChest.canBeUsed(player) ? false : super.isUseableByPlayer(player);
    }

    public void openInventory(EntityPlayer player)
    {
        if (this.associatedChest != null)
        {
            this.associatedChest.openChest();
        }

        super.openInventory(player);
    }

    public void closeInventory(EntityPlayer player)
    {
        if (this.associatedChest != null)
        {
            this.associatedChest.closeChest();
        }

        super.closeInventory(player);
        this.associatedChest = null;
    }
}
