package net.minecraft.inventory;

import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ContainerHorseInventory extends Container
{
    private IInventory horseInventory;
    private EntityHorse theHorse;

    public ContainerHorseInventory(IInventory playerInventory, final IInventory horseInventoryIn, final EntityHorse horse, EntityPlayer player)
    {
        this.horseInventory = horseInventoryIn;
        this.theHorse = horse;
        int chestRowCount = 3;
        horseInventoryIn.openInventory(player);
        int guiYOffset = (chestRowCount - 4) * 18;
        this.addSlotToContainer(new Slot(horseInventoryIn, 0, 8, 18)
        {
            public boolean isItemValid(ItemStack stack)
            {
                return super.isItemValid(stack) && stack.getItem() == Items.saddle && !this.getHasStack();
            }
        });
        this.addSlotToContainer(new Slot(horseInventoryIn, 1, 8, 36)
        {
            public boolean isItemValid(ItemStack stack)
            {
                return super.isItemValid(stack) && horse.canWearArmor() && EntityHorse.isArmorItem(stack.getItem());
            }
            public boolean canBeHovered()
            {
                return horse.canWearArmor();
            }
        });

        if (horse.isChested())
        {
            for (int chestRow = 0; chestRow < chestRowCount; ++chestRow)
            {
                for (int chestColumn = 0; chestColumn < 5; ++chestColumn)
                {
                    this.addSlotToContainer(new Slot(horseInventoryIn, 2 + chestColumn + chestRow * 5, 80 + chestColumn * 18, 18 + chestRow * 18));
                }
            }
        }

        for (int playerInventoryRow = 0; playerInventoryRow < 3; ++playerInventoryRow)
        {
            for (int playerInventoryColumn = 0; playerInventoryColumn < 9; ++playerInventoryColumn)
            {
                this.addSlotToContainer(new Slot(playerInventory, playerInventoryColumn + playerInventoryRow * 9 + 9, 8 + playerInventoryColumn * 18, 102 + playerInventoryRow * 18 + guiYOffset));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
        {
            this.addSlotToContainer(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 160 + guiYOffset));
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.horseInventory.isUseableByPlayer(playerIn) && this.theHorse.isEntityAlive() && this.theHorse.getDistanceToEntity(playerIn) < 8.0F;
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemStack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemStack = itemstack1.copy();

            if (index < this.horseInventory.getSizeInventory())
            {
                if (!this.mergeItemStack(itemstack1, this.horseInventory.getSizeInventory(), this.inventorySlots.size(), true))
                {
                    return null;
                }
            }
            else if (this.getSlot(1).isItemValid(itemstack1) && !this.getSlot(1).getHasStack())
            {
                if (!this.mergeItemStack(itemstack1, 1, 2, false))
                {
                    return null;
                }
            }
            else if (this.getSlot(0).isItemValid(itemstack1))
            {
                if (!this.mergeItemStack(itemstack1, 0, 1, false))
                {
                    return null;
                }
            }
            else if (this.horseInventory.getSizeInventory() <= 2 || !this.mergeItemStack(itemstack1, 2, this.horseInventory.getSizeInventory(), false))
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
        }

        return itemStack;
    }

    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        this.horseInventory.closeInventory(playerIn);
    }
}
