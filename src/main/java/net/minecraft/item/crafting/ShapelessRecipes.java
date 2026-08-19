package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ShapelessRecipes implements IRecipe
{
    private final ItemStack recipeOutput;
    private final List<ItemStack> recipeItems;

    public ShapelessRecipes(ItemStack output, List<ItemStack> inputList)
    {
        this.recipeOutput = output;
        this.recipeItems = inputList;
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
        List<ItemStack> remainingRecipeItems = Lists.newArrayList(this.recipeItems);

        for (int row = 0; row < inv.getHeight(); ++row)
        {
            for (int column = 0; column < inv.getWidth(); ++column)
            {
                ItemStack itemStack = inv.getStackInRowAndColumn(column, row);

                if (itemStack != null)
                {
                    boolean foundMatchingIngredient = false;

                    for (ItemStack recipeItem : remainingRecipeItems)
                    {
                        if (itemStack.getItem() == recipeItem.getItem() && (recipeItem.getMetadata() == 32767 || itemStack.getMetadata() == recipeItem.getMetadata()))
                        {
                            foundMatchingIngredient = true;
                            remainingRecipeItems.remove(recipeItem);
                            break;
                        }
                    }

                    if (!foundMatchingIngredient)
                    {
                        return false;
                    }
                }
            }
        }

        return remainingRecipeItems.isEmpty();
    }

    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return this.recipeOutput.copy();
    }

    public int getRecipeSize()
    {
        return this.recipeItems.size();
    }
}
