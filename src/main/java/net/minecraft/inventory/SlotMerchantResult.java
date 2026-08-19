package net.minecraft.inventory;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.village.MerchantRecipe;

public class SlotMerchantResult extends Slot
{
    private final InventoryMerchant theMerchantInventory;
    private EntityPlayer thePlayer;
    private int amountCrafted;
    private final IMerchant theMerchant;

    public SlotMerchantResult(EntityPlayer player, IMerchant merchant, InventoryMerchant merchantInventory, int slotIndex, int xPosition, int yPosition)
    {
        super(merchantInventory, slotIndex, xPosition, yPosition);
        this.thePlayer = player;
        this.theMerchant = merchant;
        this.theMerchantInventory = merchantInventory;
    }

    public boolean isItemValid(ItemStack stack)
    {
        return false;
    }

    public ItemStack decrStackSize(int amount)
    {
        if (this.getHasStack())
        {
            this.amountCrafted += Math.min(amount, this.getStack().stackSize);
        }

        return super.decrStackSize(amount);
    }

    protected void onCrafting(ItemStack stack, int amount)
    {
        this.amountCrafted += amount;
        this.onCrafting(stack);
    }

    protected void onCrafting(ItemStack stack)
    {
        stack.onCrafting(this.thePlayer.worldObj, this.thePlayer, this.amountCrafted);
        this.amountCrafted = 0;
    }

    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
    {
        this.onCrafting(stack);
        MerchantRecipe merchantRecipe = this.theMerchantInventory.getCurrentRecipe();

        if (merchantRecipe != null)
        {
            ItemStack itemStack = this.theMerchantInventory.getStackInSlot(0);
            ItemStack itemstack1 = this.theMerchantInventory.getStackInSlot(1);

            if (this.doTrade(merchantRecipe, itemStack, itemstack1) || this.doTrade(merchantRecipe, itemstack1, itemStack))
            {
                this.theMerchant.useRecipe(merchantRecipe);
                playerIn.triggerAchievement(StatList.timesTradedWithVillagerStat);

                if (itemStack != null && itemStack.stackSize <= 0)
                {
                    itemStack = null;
                }

                if (itemstack1 != null && itemstack1.stackSize <= 0)
                {
                    itemstack1 = null;
                }

                this.theMerchantInventory.setInventorySlotContents(0, itemStack);
                this.theMerchantInventory.setInventorySlotContents(1, itemstack1);
            }
        }
    }

    private boolean doTrade(MerchantRecipe trade, ItemStack firstItem, ItemStack secondItem)
    {
        ItemStack itemStack = trade.getItemToBuy();
        ItemStack itemstack1 = trade.getSecondItemToBuy();

        if (firstItem != null && firstItem.getItem() == itemStack.getItem())
        {
            if (itemstack1 != null && secondItem != null && itemstack1.getItem() == secondItem.getItem())
            {
                firstItem.stackSize -= itemStack.stackSize;
                secondItem.stackSize -= itemstack1.stackSize;
                return true;
            }

            if (itemstack1 == null && secondItem == null)
            {
                firstItem.stackSize -= itemStack.stackSize;
                return true;
            }
        }

        return false;
    }
}
