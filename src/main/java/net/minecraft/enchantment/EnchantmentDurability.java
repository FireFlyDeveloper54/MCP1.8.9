package net.minecraft.enchantment;

import java.util.Random;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class EnchantmentDurability extends Enchantment
{
    protected EnchantmentDurability(int enchID, ResourceLocation enchName, int enchWeight)
    {
        super(enchID, enchName, enchWeight, EnumEnchantmentType.BREAKABLE);
        this.setName("durability");
    }

    public int getMinEnchantability(int enchantmentLevel)
    {
        return 5 + (enchantmentLevel - 1) * 8;
    }

    public int getMaxEnchantability(int enchantmentLevel)
    {
        return super.getMinEnchantability(enchantmentLevel) + 50;
    }

    public int getMaxLevel()
    {
        return 3;
    }

    public boolean canApply(ItemStack stack)
    {
        return stack.isItemStackDamageable() ? true : super.canApply(stack);
    }

    public static boolean negateDamage(ItemStack stack, int level, Random random)
    {
        return stack.getItem() instanceof ItemArmor && random.nextFloat() < 0.6F ? false : random.nextInt(level + 1) > 0;
    }
}
