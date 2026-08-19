package net.minecraft.item.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class RecipesFood
{
    public void addRecipes(CraftingManager craftingManager)
    {
        craftingManager.addShapelessRecipe(new ItemStack(Items.mushroom_stew), new Object[] {Blocks.brown_mushroom, Blocks.red_mushroom, Items.bowl});
        craftingManager.addRecipe(new ItemStack(Items.cookie, 8), new Object[] {"#X#", 'X', new ItemStack(Items.dye, 1, EnumDyeColor.BROWN.getDyeDamage()), '#', Items.wheat});
        craftingManager.addRecipe(new ItemStack(Items.rabbit_stew), new Object[] {" R ", "CPM", " B ", 'R', new ItemStack(Items.cooked_rabbit), 'C', Items.carrot, 'P', Items.baked_potato, 'M', Blocks.brown_mushroom, 'B', Items.bowl});
        craftingManager.addRecipe(new ItemStack(Items.rabbit_stew), new Object[] {" R ", "CPD", " B ", 'R', new ItemStack(Items.cooked_rabbit), 'C', Items.carrot, 'P', Items.baked_potato, 'D', Blocks.red_mushroom, 'B', Items.bowl});
        craftingManager.addRecipe(new ItemStack(Blocks.melon_block), new Object[] {"MMM", "MMM", "MMM", 'M', Items.melon});
        craftingManager.addRecipe(new ItemStack(Items.melon_seeds), new Object[] {"M", 'M', Items.melon});
        craftingManager.addRecipe(new ItemStack(Items.pumpkin_seeds, 4), new Object[] {"M", 'M', Blocks.pumpkin});
        craftingManager.addShapelessRecipe(new ItemStack(Items.pumpkin_pie), new Object[] {Blocks.pumpkin, Items.sugar, Items.egg});
        craftingManager.addShapelessRecipe(new ItemStack(Items.fermented_spider_eye), new Object[] {Items.spider_eye, Blocks.brown_mushroom, Items.sugar});
        craftingManager.addShapelessRecipe(new ItemStack(Items.blaze_powder, 2), new Object[] {Items.blaze_rod});
        craftingManager.addShapelessRecipe(new ItemStack(Items.magma_cream), new Object[] {Items.blaze_powder, Items.slime_ball});
    }
}
