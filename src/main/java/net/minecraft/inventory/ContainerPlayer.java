package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

public class ContainerPlayer extends Container
{
    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
    public IInventory craftResult = new InventoryCraftResult();
    public boolean isLocalWorld;
    private final EntityPlayer thePlayer;

    public ContainerPlayer(final InventoryPlayer playerInventory, boolean localWorld, EntityPlayer player)
    {
        this.isLocalWorld = localWorld;
        this.thePlayer = player;
        this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 144, 36));

        for (int craftRow = 0; craftRow < 2; ++craftRow)
        {
            for (int craftColumn = 0; craftColumn < 2; ++craftColumn)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, craftColumn + craftRow * 2, 88 + craftColumn * 18, 26 + craftRow * 18));
            }
        }

        for (int armorSlot = 0; armorSlot < 4; ++armorSlot)
        {
            final int armorSlotIndex = armorSlot;
            this.addSlotToContainer(new Slot(playerInventory, playerInventory.getSizeInventory() - 1 - armorSlot, 8, 8 + armorSlot * 18)
            {
                public int getSlotStackLimit()
                {
                    return 1;
                }
                public boolean isItemValid(ItemStack stack)
                {
                    return stack == null ? false : (stack.getItem() instanceof ItemArmor ? ((ItemArmor)stack.getItem()).armorType == armorSlotIndex : (stack.getItem() != Item.getItemFromBlock(Blocks.pumpkin) && stack.getItem() != Items.skull ? false : armorSlotIndex == 0));
                }
                public String getSlotTexture()
                {
                    return ItemArmor.EMPTY_SLOT_NAMES[armorSlotIndex];
                }
            });
        }

        for (int playerInventoryRow = 0; playerInventoryRow < 3; ++playerInventoryRow)
        {
            for (int playerInventoryColumn = 0; playerInventoryColumn < 9; ++playerInventoryColumn)
            {
                this.addSlotToContainer(new Slot(playerInventory, playerInventoryColumn + (playerInventoryRow + 1) * 9, 8 + playerInventoryColumn * 18, 84 + playerInventoryRow * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
        {
            this.addSlotToContainer(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 142));
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.thePlayer.worldObj));
    }

    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        for (int craftingSlot = 0; craftingSlot < 4; ++craftingSlot)
        {
            ItemStack itemStack = this.craftMatrix.removeStackFromSlot(craftingSlot);

            if (itemStack != null)
            {
                playerIn.dropPlayerItemWithRandomChoice(itemStack, false);
            }
        }

        this.craftResult.setInventorySlotContents(0, (ItemStack)null);
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
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
                if (!this.mergeItemStack(itemstack1, 9, 45, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemStack);
            }
            else if (index >= 1 && index < 5)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, false))
                {
                    return null;
                }
            }
            else if (index >= 5 && index < 9)
            {
                if (!this.mergeItemStack(itemstack1, 9, 45, false))
                {
                    return null;
                }
            }
            else if (itemStack.getItem() instanceof ItemArmor && !((Slot)this.inventorySlots.get(5 + ((ItemArmor)itemStack.getItem()).armorType)).getHasStack())
            {
                int armorInventorySlot = 5 + ((ItemArmor)itemStack.getItem()).armorType;

                if (!this.mergeItemStack(itemstack1, armorInventorySlot, armorInventorySlot + 1, false))
                {
                    return null;
                }
            }
            else if (index >= 9 && index < 36)
            {
                if (!this.mergeItemStack(itemstack1, 36, 45, false))
                {
                    return null;
                }
            }
            else if (index >= 36 && index < 45)
            {
                if (!this.mergeItemStack(itemstack1, 9, 36, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 9, 45, false))
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

    public boolean canMergeSlot(ItemStack stack, Slot slotIn)
    {
        return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
    }
}
