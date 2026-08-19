package net.minecraft.enchantment;

import net.minecraft.util.ResourceLocation;

public class EnchantmentOxygen extends Enchantment
{
    public EnchantmentOxygen(int enchID, ResourceLocation enchName, int enchWeight)
    {
        super(enchID, enchName, enchWeight, EnumEnchantmentType.ARMOR_HEAD);
        this.setName("oxygen");
    }

    public int getMinEnchantability(int enchantmentLevel)
    {
        return 10 * enchantmentLevel;
    }

    public int getMaxEnchantability(int enchantmentLevel)
    {
        return this.getMinEnchantability(enchantmentLevel) + 30;
    }

    public int getMaxLevel()
    {
        return 3;
    }
}
