package net.minecraft.item;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.StatCollector;

public class ItemFireworkCharge extends Item
{
    public int getColorFromItemStack(ItemStack stack, int renderPass)
    {
        if (renderPass != 1)
        {
            return super.getColorFromItemStack(stack, renderPass);
        }
        else
        {
            NBTBase colorsTag = getExplosionTag(stack, "Colors");

            if (!(colorsTag instanceof NBTTagIntArray))
            {
                return 9079434;
            }
            else
            {
                NBTTagIntArray colorArrayTag = (NBTTagIntArray)colorsTag;
                int[] colors = colorArrayTag.getIntArray();

                if (colors.length == 1)
                {
                    return colors[0];
                }
                else
                {
                    int redTotal = 0;
                    int greenTotal = 0;
                    int blueTotal = 0;

                    for (int color : colors)
                    {
                        redTotal += (color & 16711680) >> 16;
                        greenTotal += (color & 65280) >> 8;
                        blueTotal += (color & 255) >> 0;
                    }

                    redTotal = redTotal / colors.length;
                    greenTotal = greenTotal / colors.length;
                    blueTotal = blueTotal / colors.length;
                    return redTotal << 16 | greenTotal << 8 | blueTotal;
                }
            }
        }
    }

    public static NBTBase getExplosionTag(ItemStack stack, String key)
    {
        if (stack.hasTagCompound())
        {
            NBTTagCompound explosionTag = stack.getTagCompound().getCompoundTag("Explosion");

            if (explosionTag != null)
            {
                return explosionTag.getTag(key);
            }
        }

        return null;
    }

    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        if (stack.hasTagCompound())
        {
            NBTTagCompound explosionTag = stack.getTagCompound().getCompoundTag("Explosion");

            if (explosionTag != null)
            {
                addExplosionInfo(explosionTag, tooltip);
            }
        }
    }

    public static void addExplosionInfo(NBTTagCompound nbt, List<String> tooltip)
    {
        byte explosionType = nbt.getByte("Type");

        if (explosionType >= 0 && explosionType <= 4)
        {
            tooltip.add(StatCollector.translateToLocal("item.fireworksCharge.type." + explosionType).trim());
        }
        else
        {
            tooltip.add(StatCollector.translateToLocal("item.fireworksCharge.type").trim());
        }

        int[] colors = nbt.getIntArray("Colors");

        if (colors.length > 0)
        {
            boolean firstColor = true;
            String colorText = "";

            for (int color : colors)
            {
                if (!firstColor)
                {
                    colorText = colorText + ", ";
                }

                firstColor = false;
                boolean matchedDyeColor = false;

                for (int dyeIndex = 0; dyeIndex < ItemDye.dyeColors.length; ++dyeIndex)
                {
                    if (color == ItemDye.dyeColors[dyeIndex])
                    {
                        matchedDyeColor = true;
                        colorText = colorText + StatCollector.translateToLocal("item.fireworksCharge." + EnumDyeColor.byDyeDamage(dyeIndex).getUnlocalizedName());
                        break;
                    }
                }

                if (!matchedDyeColor)
                {
                    colorText = colorText + StatCollector.translateToLocal("item.fireworksCharge.customColor");
                }
            }

            tooltip.add(colorText);
        }

        int[] fadeColors = nbt.getIntArray("FadeColors");

        if (fadeColors.length > 0)
        {
            boolean firstFadeColor = true;
            String fadeText = StatCollector.translateToLocal("item.fireworksCharge.fadeTo") + " ";

            for (int fadeColor : fadeColors)
            {
                if (!firstFadeColor)
                {
                    fadeText = fadeText + ", ";
                }

                firstFadeColor = false;
                boolean matchedFadeColor = false;

                for (int dyeIndex = 0; dyeIndex < 16; ++dyeIndex)
                {
                    if (fadeColor == ItemDye.dyeColors[dyeIndex])
                    {
                        matchedFadeColor = true;
                        fadeText = fadeText + StatCollector.translateToLocal("item.fireworksCharge." + EnumDyeColor.byDyeDamage(dyeIndex).getUnlocalizedName());
                        break;
                    }
                }

                if (!matchedFadeColor)
                {
                    fadeText = fadeText + StatCollector.translateToLocal("item.fireworksCharge.customColor");
                }
            }

            tooltip.add(fadeText);
        }

        boolean hasTrail = nbt.getBoolean("Trail");

        if (hasTrail)
        {
            tooltip.add(StatCollector.translateToLocal("item.fireworksCharge.trail"));
        }

        boolean hasFlicker = nbt.getBoolean("Flicker");

        if (hasFlicker)
        {
            tooltip.add(StatCollector.translateToLocal("item.fireworksCharge.flicker"));
        }
    }
}
