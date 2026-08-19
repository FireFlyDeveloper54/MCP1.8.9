package net.minecraft.item.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ShapedRecipes implements IRecipe
{
    private final int recipeWidth;
    private final int recipeHeight;
    private final ItemStack[] recipeItems;
    private final ItemStack recipeOutput;
    private boolean copyIngredientNBT;

    public ShapedRecipes(int width, int height, ItemStack[] recipeItemsIn, ItemStack output)
    {
        this.recipeWidth = width;
        this.recipeHeight = height;
        this.recipeItems = recipeItemsIn;
        this.recipeOutput = output;
    }

    public ItemStack getRecipeOutput()
    {
        return this.recipeOutput;
    }

    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        ItemStack[] remainingItems = new ItemStack[inv.getSizeInventory()];

        for (int slotIndex = 0; slotIndex < remainingItems.length; ++slotIndex)
        {
            ItemStack itemStack = inv.getStackInSlot(slotIndex);

            if (itemStack != null && itemStack.getItem().hasContainerItem())
            {
                remainingItems[slotIndex] = new ItemStack(itemStack.getItem().getContainerItem());
            }
        }

        return remainingItems;
    }

    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        for (int offsetX = 0; offsetX <= 3 - this.recipeWidth; ++offsetX)
        {
            for (int offsetY = 0; offsetY <= 3 - this.recipeHeight; ++offsetY)
            {
                if (this.checkMatch(inv, offsetX, offsetY, true))
                {
                    return true;
                }

                if (this.checkMatch(inv, offsetX, offsetY, false))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkMatch(InventoryCrafting inventory, int recipeOffsetX, int recipeOffsetY, boolean mirrored)
    {
        for (int column = 0; column < 3; ++column)
        {
            for (int row = 0; row < 3; ++row)
            {
                int recipeX = column - recipeOffsetX;
                int recipeY = row - recipeOffsetY;
                ItemStack expectedStack = null;

                if (recipeX >= 0 && recipeY >= 0 && recipeX < this.recipeWidth && recipeY < this.recipeHeight)
                {
                    if (mirrored)
                    {
                        expectedStack = this.recipeItems[this.recipeWidth - recipeX - 1 + recipeY * this.recipeWidth];
                    }
                    else
                    {
                        expectedStack = this.recipeItems[recipeX + recipeY * this.recipeWidth];
                    }
                }

                ItemStack actualStack = inventory.getStackInRowAndColumn(column, row);

                if (actualStack != null || expectedStack != null)
                {
                    if (actualStack == null && expectedStack != null || actualStack != null && expectedStack == null)
                    {
                        return false;
                    }

                    if (expectedStack.getItem() != actualStack.getItem())
                    {
                        return false;
                    }

                    if (expectedStack.getMetadata() != 32767 && expectedStack.getMetadata() != actualStack.getMetadata())
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack itemStack = this.getRecipeOutput().copy();

        if (this.copyIngredientNBT)
        {
            for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
            {
                ItemStack slotStack = inv.getStackInSlot(slotIndex);

                if (slotStack != null && slotStack.hasTagCompound())
                {
                    itemStack.setTagCompound((NBTTagCompound)slotStack.getTagCompound().copy());
                }
            }
        }

        return itemStack;
    }

    public int getRecipeSize()
    {
        return this.recipeWidth * this.recipeHeight;
    }
}
