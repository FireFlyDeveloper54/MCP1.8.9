package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ContainerDispenser extends Container
{
    private IInventory dispenserInventory;

    public ContainerDispenser(IInventory playerInventory, IInventory dispenserInventoryIn)
    {
        this.dispenserInventory = dispenserInventoryIn;

        for (int dispenserRow = 0; dispenserRow < 3; ++dispenserRow)
        {
            for (int dispenserColumn = 0; dispenserColumn < 3; ++dispenserColumn)
            {
                this.addSlotToContainer(new Slot(dispenserInventoryIn, dispenserColumn + dispenserRow * 3, 62 + dispenserColumn * 18, 17 + dispenserRow * 18));
            }
        }

        for (int playerInventoryRow = 0; playerInventoryRow < 3; ++playerInventoryRow)
        {
            for (int playerInventoryColumn = 0; playerInventoryColumn < 9; ++playerInventoryColumn)
            {
                this.addSlotToContainer(new Slot(playerInventory, playerInventoryColumn + playerInventoryRow * 9 + 9, 8 + playerInventoryColumn * 18, 84 + playerInventoryRow * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
        {
            this.addSlotToContainer(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 142));
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.dispenserInventory.isUseableByPlayer(playerIn);
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemStack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemStack = itemstack1.copy();

            if (index < 9)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 9, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemStack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemStack;
    }
}
