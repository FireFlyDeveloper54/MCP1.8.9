package net.minecraft.item.crafting;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RecipesMapCloning implements IRecipe
{
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        int blankMapCount = 0;
        ItemStack filledMapStack = null;

        for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
        {
            ItemStack slotStack = inv.getStackInSlot(slotIndex);

            if (slotStack != null)
            {
                if (slotStack.getItem() == Items.filled_map)
                {
                    if (filledMapStack != null)
                    {
                        return false;
                    }

                    filledMapStack = slotStack;
                }
                else
                {
                    if (slotStack.getItem() != Items.map)
                    {
                        return false;
                    }

                    ++blankMapCount;
                }
            }
        }

        return filledMapStack != null && blankMapCount > 0;
    }

    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        int blankMapCount = 0;
        ItemStack filledMapStack = null;

        for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
        {
            ItemStack slotStack = inv.getStackInSlot(slotIndex);

            if (slotStack != null)
            {
                if (slotStack.getItem() == Items.filled_map)
                {
                    if (filledMapStack != null)
                    {
                        return null;
                    }

                    filledMapStack = slotStack;
                }
                else
                {
                    if (slotStack.getItem() != Items.map)
                    {
                        return null;
                    }

                    ++blankMapCount;
                }
            }
        }

        if (filledMapStack != null && blankMapCount >= 1)
        {
            ItemStack clonedMapStack = new ItemStack(Items.filled_map, blankMapCount + 1, filledMapStack.getMetadata());

            if (filledMapStack.hasDisplayName())
            {
                clonedMapStack.setStackDisplayName(filledMapStack.getDisplayName());
            }

            return clonedMapStack;
        }
        else
        {
            return null;
        }
    }

    public int getRecipeSize()
    {
        return 9;
    }

    public ItemStack getRecipeOutput()
    {
        return null;
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
}
