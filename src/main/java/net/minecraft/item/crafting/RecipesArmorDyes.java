package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RecipesArmorDyes implements IRecipe
{
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        ItemStack armorStack = null;
        List<ItemStack> dyeStacks = Lists.<ItemStack>newArrayList();

        for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
        {
            ItemStack slotStack = inv.getStackInSlot(slotIndex);

            if (slotStack != null)
            {
                if (slotStack.getItem() instanceof ItemArmor)
                {
                    ItemArmor armorItem = (ItemArmor)slotStack.getItem();

                    if (armorItem.getArmorMaterial() != ItemArmor.ArmorMaterial.LEATHER || armorStack != null)
                    {
                        return false;
                    }

                    armorStack = slotStack;
                }
                else
                {
                    if (slotStack.getItem() != Items.dye)
                    {
                        return false;
                    }

                    dyeStacks.add(slotStack);
                }
            }
        }

        return armorStack != null && !dyeStacks.isEmpty();
    }

    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack dyedArmorStack = null;
        int[] channelTotals = new int[3];
        int totalMaxChannel = 0;
        int colorSampleCount = 0;
        ItemArmor armorItem = null;

        for (int slotIndex = 0; slotIndex < inv.getSizeInventory(); ++slotIndex)
        {
            ItemStack slotStack = inv.getStackInSlot(slotIndex);

            if (slotStack != null)
            {
                if (slotStack.getItem() instanceof ItemArmor)
                {
                    armorItem = (ItemArmor)slotStack.getItem();

                    if (armorItem.getArmorMaterial() != ItemArmor.ArmorMaterial.LEATHER || dyedArmorStack != null)
                    {
                        return null;
                    }

                    dyedArmorStack = slotStack.copy();
                    dyedArmorStack.stackSize = 1;

                    if (armorItem.hasColor(slotStack))
                    {
                        int armorColor = armorItem.getColor(dyedArmorStack);
                        float armorRed = (float)(armorColor >> 16 & 255) / 255.0F;
                        float armorGreen = (float)(armorColor >> 8 & 255) / 255.0F;
                        float armorBlue = (float)(armorColor & 255) / 255.0F;
                        totalMaxChannel = (int)((float)totalMaxChannel + Math.max(armorRed, Math.max(armorGreen, armorBlue)) * 255.0F);
                        channelTotals[0] = (int)((float)channelTotals[0] + armorRed * 255.0F);
                        channelTotals[1] = (int)((float)channelTotals[1] + armorGreen * 255.0F);
                        channelTotals[2] = (int)((float)channelTotals[2] + armorBlue * 255.0F);
                        ++colorSampleCount;
                    }
                }
                else
                {
                    if (slotStack.getItem() != Items.dye)
                    {
                        return null;
                    }

                    float[] dyeRgb = EntitySheep.getDyeRgb(EnumDyeColor.byDyeDamage(slotStack.getMetadata()));
                    int dyeRed = (int)(dyeRgb[0] * 255.0F);
                    int dyeGreen = (int)(dyeRgb[1] * 255.0F);
                    int dyeBlue = (int)(dyeRgb[2] * 255.0F);
                    totalMaxChannel += Math.max(dyeRed, Math.max(dyeGreen, dyeBlue));
                    channelTotals[0] += dyeRed;
                    channelTotals[1] += dyeGreen;
                    channelTotals[2] += dyeBlue;
                    ++colorSampleCount;
                }
            }
        }

        if (armorItem == null)
        {
            return null;
        }
        else
        {
            int mixedRed = channelTotals[0] / colorSampleCount;
            int mixedGreen = channelTotals[1] / colorSampleCount;
            int mixedBlue = channelTotals[2] / colorSampleCount;
            float averageMaxChannel = (float)totalMaxChannel / (float)colorSampleCount;
            float maxMixedChannel = (float)Math.max(mixedRed, Math.max(mixedGreen, mixedBlue));
            mixedRed = (int)((float)mixedRed * averageMaxChannel / maxMixedChannel);
            mixedGreen = (int)((float)mixedGreen * averageMaxChannel / maxMixedChannel);
            mixedBlue = (int)((float)mixedBlue * averageMaxChannel / maxMixedChannel);
            int color = (mixedRed << 8) + mixedGreen;
            color = (color << 8) + mixedBlue;
            armorItem.setColor(dyedArmorStack, color);
            return dyedArmorStack;
        }
    }

    public int getRecipeSize()
    {
        return 10;
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
