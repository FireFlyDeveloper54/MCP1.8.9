package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.AchievementList;

public class ContainerBrewingStand extends Container
{
    private IInventory tileBrewingStand;
    private final Slot theSlot;
    private int brewTime;

    public ContainerBrewingStand(InventoryPlayer playerInventory, IInventory tileBrewingStandIn)
    {
        this.tileBrewingStand = tileBrewingStandIn;
        this.addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 0, 56, 46));
        this.addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 1, 79, 53));
        this.addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 2, 102, 46));
        this.theSlot = this.addSlotToContainer(new ContainerBrewingStand.Ingredient(tileBrewingStandIn, 3, 79, 17));

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

    public void onCraftGuiOpened(ICrafting listener)
    {
        super.onCraftGuiOpened(listener);
        listener.sendAllWindowProperties(this, this.tileBrewingStand);
    }

    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (int crafterIndex = 0; crafterIndex < this.crafters.size(); ++crafterIndex)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(crafterIndex);

            if (this.brewTime != this.tileBrewingStand.getField(0))
            {
                icrafting.sendProgressBarUpdate(this, 0, this.tileBrewingStand.getField(0));
            }
        }

        this.brewTime = this.tileBrewingStand.getField(0);
    }

    public void updateProgressBar(int id, int data)
    {
        this.tileBrewingStand.setField(id, data);
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.tileBrewingStand.isUseableByPlayer(playerIn);
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemStack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemStack = itemstack1.copy();

            if ((index < 0 || index > 2) && index != 3)
            {
                if (!this.theSlot.getHasStack() && this.theSlot.isItemValid(itemstack1))
                {
                    if (!this.mergeItemStack(itemstack1, 3, 4, false))
                    {
                        return null;
                    }
                }
                else if (ContainerBrewingStand.Potion.canHoldPotion(itemStack))
                {
                    if (!this.mergeItemStack(itemstack1, 0, 3, false))
                    {
                        return null;
                    }
                }
                else if (index >= 4 && index < 31)
                {
                    if (!this.mergeItemStack(itemstack1, 31, 40, false))
                    {
                        return null;
                    }
                }
                else if (index >= 31 && index < 40)
                {
                    if (!this.mergeItemStack(itemstack1, 4, 31, false))
                    {
                        return null;
                    }
                }
                else if (!this.mergeItemStack(itemstack1, 4, 40, false))
                {
                    return null;
                }
            }
            else
            {
                if (!this.mergeItemStack(itemstack1, 4, 40, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemStack);
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

    class Ingredient extends Slot
    {
        public Ingredient(IInventory inventoryIn, int index, int xPosition, int yPosition)
        {
            super(inventoryIn, index, xPosition, yPosition);
        }

        public boolean isItemValid(ItemStack stack)
        {
            return stack != null ? stack.getItem().isPotionIngredient(stack) : false;
        }

        public int getSlotStackLimit()
        {
            return 64;
        }
    }

    static class Potion extends Slot
    {
        private EntityPlayer player;

        public Potion(EntityPlayer playerIn, IInventory inventoryIn, int index, int xPosition, int yPosition)
        {
            super(inventoryIn, index, xPosition, yPosition);
            this.player = playerIn;
        }

        public boolean isItemValid(ItemStack stack)
        {
            return canHoldPotion(stack);
        }

        public int getSlotStackLimit()
        {
            return 1;
        }

        public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
        {
            if (stack.getItem() == Items.potionitem && stack.getMetadata() > 0)
            {
                this.player.triggerAchievement(AchievementList.potion);
            }

            super.onPickupFromSlot(playerIn, stack);
        }

        public static boolean canHoldPotion(ItemStack stack)
        {
            return stack != null && (stack.getItem() == Items.potionitem || stack.getItem() == Items.glass_bottle);
        }
    }
}
