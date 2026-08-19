package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.stats.AchievementList;

public class SlotCrafting extends Slot
{
    private final InventoryCrafting craftMatrix;
    private final EntityPlayer thePlayer;
    private int amountCrafted;

    public SlotCrafting(EntityPlayer player, InventoryCrafting craftingInventory, IInventory resultInventory, int slotIndex, int xPosition, int yPosition)
    {
        super(resultInventory, slotIndex, xPosition, yPosition);
        this.thePlayer = player;
        this.craftMatrix = craftingInventory;
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
        if (this.amountCrafted > 0)
        {
            stack.onCrafting(this.thePlayer.worldObj, this.thePlayer, this.amountCrafted);
        }

        this.amountCrafted = 0;

        if (stack.getItem() == Item.getItemFromBlock(Blocks.crafting_table))
        {
            this.thePlayer.triggerAchievement(AchievementList.buildWorkBench);
        }

        if (stack.getItem() instanceof ItemPickaxe)
        {
            this.thePlayer.triggerAchievement(AchievementList.buildPickaxe);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.furnace))
        {
            this.thePlayer.triggerAchievement(AchievementList.buildFurnace);
        }

        if (stack.getItem() instanceof ItemHoe)
        {
            this.thePlayer.triggerAchievement(AchievementList.buildHoe);
        }

        if (stack.getItem() == Items.bread)
        {
            this.thePlayer.triggerAchievement(AchievementList.makeBread);
        }

        if (stack.getItem() == Items.cake)
        {
            this.thePlayer.triggerAchievement(AchievementList.bakeCake);
        }

        if (stack.getItem() instanceof ItemPickaxe && ((ItemPickaxe)stack.getItem()).getToolMaterial() != Item.ToolMaterial.WOOD)
        {
            this.thePlayer.triggerAchievement(AchievementList.buildBetterPickaxe);
        }

        if (stack.getItem() instanceof ItemSword)
        {
            this.thePlayer.triggerAchievement(AchievementList.buildSword);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.enchanting_table))
        {
            this.thePlayer.triggerAchievement(AchievementList.enchantments);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.bookshelf))
        {
            this.thePlayer.triggerAchievement(AchievementList.bookcase);
        }

        if (stack.getItem() == Items.golden_apple && stack.getMetadata() == 1)
        {
            this.thePlayer.triggerAchievement(AchievementList.overpowered);
        }
    }

    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
    {
        this.onCrafting(stack);
        ItemStack[] aitemstack = CraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);

        for (int slotIndex = 0; slotIndex < aitemstack.length; ++slotIndex)
        {
            ItemStack itemStack = this.craftMatrix.getStackInSlot(slotIndex);
            ItemStack remainingStack = aitemstack[slotIndex];

            if (itemStack != null)
            {
                this.craftMatrix.decrStackSize(slotIndex, 1);
            }

            if (remainingStack != null)
            {
                if (this.craftMatrix.getStackInSlot(slotIndex) == null)
                {
                    this.craftMatrix.setInventorySlotContents(slotIndex, remainingStack);
                }
                else if (!this.thePlayer.inventory.addItemStackToInventory(remainingStack))
                {
                    this.thePlayer.dropPlayerItemWithRandomChoice(remainingStack, false);
                }
            }
        }
    }
}
