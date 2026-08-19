package net.minecraft.enchantment;

import net.minecraft.util.ResourceLocation;

public class EnchantmentLootBonus extends Enchantment
{
    protected EnchantmentLootBonus(int enchID, ResourceLocation enchName, int enchWeight, EnumEnchantmentType enchantmentType)
    {
        super(enchID, enchName, enchWeight, enchantmentType);

        if (enchantmentType == EnumEnchantmentType.DIGGER)
        {
            this.setName("lootBonusDigger");
        }
        else if (enchantmentType == EnumEnchantmentType.FISHING_ROD)
        {
            this.setName("lootBonusFishing");
        }
        else
        {
            this.setName("lootBonus");
        }
    }

    public int getMinEnchantability(int enchantmentLevel)
    {
        return 15 + (enchantmentLevel - 1) * 9;
    }

    public int getMaxEnchantability(int enchantmentLevel)
    {
        return super.getMinEnchantability(enchantmentLevel) + 50;
    }

    public int getMaxLevel()
    {
        return 3;
    }

    public boolean canApplyTogether(Enchantment ench)
    {
        return super.canApplyTogether(ench) && ench.effectId != silkTouch.effectId;
    }
}
