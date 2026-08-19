package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class RecipeFireworks implements IRecipe
{
    private ItemStack recipeOutput;

    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        this.recipeOutput = null;
        int paperCount = 0;
        int gunpowderCount = 0;
        int dyeCount = 0;
        int fireworkChargeCount = 0;
        int effectIngredientCount = 0;
        int shapeIngredientCount = 0;

        for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
        {
            ItemStack itemStack = inv.getStackInSlot(slotIndex);

            if (itemStack != null)
            {
                if (itemStack.getItem() == Items.gunpowder)
                {
                    ++gunpowderCount;
                }
                else if (itemStack.getItem() == Items.firework_charge)
                {
                    ++fireworkChargeCount;
                }
                else if (itemStack.getItem() == Items.dye)
                {
                    ++dyeCount;
                }
                else if (itemStack.getItem() == Items.paper)
                {
                    ++paperCount;
                }
                else if (itemStack.getItem() == Items.glowstone_dust)
                {
                    ++effectIngredientCount;
                }
                else if (itemStack.getItem() == Items.diamond)
                {
                    ++effectIngredientCount;
                }
                else if (itemStack.getItem() == Items.fire_charge)
                {
                    ++shapeIngredientCount;
                }
                else if (itemStack.getItem() == Items.feather)
                {
                    ++shapeIngredientCount;
                }
                else if (itemStack.getItem() == Items.gold_nugget)
                {
                    ++shapeIngredientCount;
                }
                else
                {
                    if (itemStack.getItem() != Items.skull)
                    {
                        return false;
                    }

                    ++shapeIngredientCount;
                }
            }
        }

        effectIngredientCount = effectIngredientCount + dyeCount + shapeIngredientCount;

        if (gunpowderCount <= 3 && paperCount <= 1)
        {
            if (gunpowderCount >= 1 && paperCount == 1 && effectIngredientCount == 0)
            {
                this.recipeOutput = new ItemStack(Items.fireworks);

                if (fireworkChargeCount > 0)
                {
                    NBTTagCompound rocketTag = new NBTTagCompound();
                    NBTTagCompound fireworksTag = new NBTTagCompound();
                    NBTTagList explosionList = new NBTTagList();

                    for (int index = 0; index < inv.getSizeInventory(); ++index)
                    {
                        ItemStack chargeStack = inv.getStackInSlot(index);

                        if (chargeStack != null && chargeStack.getItem() == Items.firework_charge && chargeStack.hasTagCompound() && chargeStack.getTagCompound().hasKey("Explosion", 10))
                        {
                            explosionList.appendTag(chargeStack.getTagCompound().getCompoundTag("Explosion"));
                        }
                    }

                    fireworksTag.setTag("Explosions", explosionList);
                    fireworksTag.setByte("Flight", (byte)gunpowderCount);
                    rocketTag.setTag("Fireworks", fireworksTag);
                    this.recipeOutput.setTagCompound(rocketTag);
                }

                return true;
            }
            else if (gunpowderCount == 1 && paperCount == 0 && fireworkChargeCount == 0 && dyeCount > 0 && shapeIngredientCount <= 1)
            {
                this.recipeOutput = new ItemStack(Items.firework_charge);
                NBTTagCompound rootTag = new NBTTagCompound();
                NBTTagCompound explosionTag = new NBTTagCompound();
                byte explosionType = 0;
                List<Integer> colorList = Lists.<Integer>newArrayList();

                for (int outerIndex = 0; outerIndex < inv.getSizeInventory(); ++outerIndex)
                {
                    ItemStack ingredientStack = inv.getStackInSlot(outerIndex);

                    if (ingredientStack != null)
                    {
                        if (ingredientStack.getItem() == Items.dye)
                        {
                            colorList.add(Integer.valueOf(ItemDye.dyeColors[ingredientStack.getMetadata() & 15]));
                        }
                        else if (ingredientStack.getItem() == Items.glowstone_dust)
                        {
                            explosionTag.setBoolean("Flicker", true);
                        }
                        else if (ingredientStack.getItem() == Items.diamond)
                        {
                            explosionTag.setBoolean("Trail", true);
                        }
                        else if (ingredientStack.getItem() == Items.fire_charge)
                        {
                            explosionType = 1;
                        }
                        else if (ingredientStack.getItem() == Items.feather)
                        {
                            explosionType = 4;
                        }
                        else if (ingredientStack.getItem() == Items.gold_nugget)
                        {
                            explosionType = 2;
                        }
                        else if (ingredientStack.getItem() == Items.skull)
                        {
                            explosionType = 3;
                        }
                    }
                }

                int[] colors = new int[colorList.size()];

                for (int colorIndex = 0; colorIndex < colors.length; ++colorIndex)
                {
                    colors[colorIndex] = colorList.get(colorIndex).intValue();
                }

                explosionTag.setIntArray("Colors", colors);
                explosionTag.setByte("Type", explosionType);
                rootTag.setTag("Explosion", explosionTag);
                this.recipeOutput.setTagCompound(rootTag);
                return true;
            }
            else if (gunpowderCount == 0 && paperCount == 0 && fireworkChargeCount == 1 && dyeCount > 0 && dyeCount == effectIngredientCount)
            {
                List<Integer> fadeColorList = Lists.<Integer>newArrayList();

                for (int inventoryIndex = 0; inventoryIndex < inv.getSizeInventory(); ++inventoryIndex)
                {
                    ItemStack ingredientStack = inv.getStackInSlot(inventoryIndex);

                    if (ingredientStack != null)
                    {
                        if (ingredientStack.getItem() == Items.dye)
                        {
                            fadeColorList.add(Integer.valueOf(ItemDye.dyeColors[ingredientStack.getMetadata() & 15]));
                        }
                        else if (ingredientStack.getItem() == Items.firework_charge)
                        {
                            this.recipeOutput = ingredientStack.copy();
                            this.recipeOutput.stackSize = 1;
                        }
                    }
                }

                int[] fadeColors = new int[fadeColorList.size()];

                for (int fadeColorIndex = 0; fadeColorIndex < fadeColors.length; ++fadeColorIndex)
                {
                    fadeColors[fadeColorIndex] = fadeColorList.get(fadeColorIndex).intValue();
                }

                if (this.recipeOutput != null && this.recipeOutput.hasTagCompound())
                {
                    NBTTagCompound explosionTag = this.recipeOutput.getTagCompound().getCompoundTag("Explosion");

                    if (explosionTag == null)
                    {
                        return false;
                    }
                    else
                    {
                        explosionTag.setIntArray("FadeColors", fadeColors);
                        return true;
                    }
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return this.recipeOutput.copy();
    }

    public int getRecipeSize()
    {
        return 10;
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
}
