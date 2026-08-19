package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ContainerBeacon extends Container
{
    private IInventory tileBeacon;
    private final ContainerBeacon.BeaconSlot beaconSlot;

    public ContainerBeacon(IInventory playerInventory, IInventory tileBeaconIn)
    {
        this.tileBeacon = tileBeaconIn;
        this.addSlotToContainer(this.beaconSlot = new ContainerBeacon.BeaconSlot(tileBeaconIn, 0, 136, 110));
        int inventoryLeft = 36;
        int inventoryTop = 137;

        for (int playerInventoryRow = 0; playerInventoryRow < 3; ++playerInventoryRow)
        {
            for (int playerInventoryColumn = 0; playerInventoryColumn < 9; ++playerInventoryColumn)
            {
                this.addSlotToContainer(new Slot(playerInventory, playerInventoryColumn + playerInventoryRow * 9 + 9, inventoryLeft + playerInventoryColumn * 18, inventoryTop + playerInventoryRow * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
        {
            this.addSlotToContainer(new Slot(playerInventory, hotbarSlot, inventoryLeft + hotbarSlot * 18, 58 + inventoryTop));
        }
    }

    public void onCraftGuiOpened(ICrafting listener)
    {
        super.onCraftGuiOpened(listener);
        listener.sendAllWindowProperties(this, this.tileBeacon);
    }

    public void updateProgressBar(int id, int data)
    {
        this.tileBeacon.setField(id, data);
    }

    public IInventory getBeaconInventory()
    {
        return this.tileBeacon;
    }

    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (playerIn != null && !playerIn.worldObj.isRemote)
        {
            ItemStack itemStack = this.beaconSlot.decrStackSize(this.beaconSlot.getSlotStackLimit());

            if (itemStack != null)
            {
                playerIn.dropPlayerItemWithRandomChoice(itemStack, false);
            }
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.tileBeacon.isUseableByPlayer(playerIn);
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemStack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemStack = itemstack1.copy();

            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 1, 37, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemStack);
            }
            else if (!this.beaconSlot.getHasStack() && this.beaconSlot.isItemValid(itemstack1) && itemstack1.stackSize == 1)
            {
                if (!this.mergeItemStack(itemstack1, 0, 1, false))
                {
                    return null;
                }
            }
            else if (index >= 1 && index < 28)
            {
                if (!this.mergeItemStack(itemstack1, 28, 37, false))
                {
                    return null;
                }
            }
            else if (index >= 28 && index < 37)
            {
                if (!this.mergeItemStack(itemstack1, 1, 28, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 1, 37, false))
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

    class BeaconSlot extends Slot
    {
        public BeaconSlot(IInventory inventoryIn, int slotIndex, int xPosition, int yPosition)
        {
            super(inventoryIn, slotIndex, xPosition, yPosition);
        }

        public boolean isItemValid(ItemStack stack)
        {
            return stack == null ? false : stack.getItem() == Items.emerald || stack.getItem() == Items.diamond || stack.getItem() == Items.gold_ingot || stack.getItem() == Items.iron_ingot;
        }

        public int getSlotStackLimit()
        {
            return 1;
        }
    }
}
