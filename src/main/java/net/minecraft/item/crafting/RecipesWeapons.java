package net.minecraft.item.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RecipesWeapons
{
    private String[][] recipePatterns = new String[][] {{"X", "X", "#"}};
    private Object[][] recipeItems = new Object[][] {{Blocks.planks, Blocks.cobblestone, Items.iron_ingot, Items.diamond, Items.gold_ingot}, {Items.wooden_sword, Items.stone_sword, Items.iron_sword, Items.diamond_sword, Items.golden_sword}};

    public void addRecipes(CraftingManager craftingManager)
    {
        for (int materialIndex = 0; materialIndex < this.recipeItems[0].length; ++materialIndex)
        {
            Object material = this.recipeItems[0][materialIndex];

            for (int weaponTypeIndex = 0; weaponTypeIndex < this.recipeItems.length - 1; ++weaponTypeIndex)
            {
                Item weaponItem = (Item)this.recipeItems[weaponTypeIndex + 1][materialIndex];
                craftingManager.addRecipe(new ItemStack(weaponItem), new Object[] {this.recipePatterns[weaponTypeIndex], '#', Items.stick, 'X', material});
            }
        }

        craftingManager.addRecipe(new ItemStack(Items.bow, 1), new Object[] {" #X", "# X", " #X", 'X', Items.string, '#', Items.stick});
        craftingManager.addRecipe(new ItemStack(Items.arrow, 4), new Object[] {"X", "#", "Y", 'Y', Items.feather, 'X', Items.flint, '#', Items.stick});
    }
}
