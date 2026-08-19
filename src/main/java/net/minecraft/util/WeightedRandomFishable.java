package net.minecraft.util;

import java.util.Random;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

public class WeightedRandomFishable extends WeightedRandom.Item
{
    private final ItemStack returnStack;
    private float maxDamagePercent;
    private boolean enchantable;

    public WeightedRandomFishable(ItemStack returnStackIn, int itemWeightIn)
    {
        super(itemWeightIn);
        this.returnStack = returnStackIn;
    }

    public ItemStack getItemStack(Random random)
    {
        ItemStack itemStack = this.returnStack.copy();

        if (this.maxDamagePercent > 0.0F)
        {
            int maxDamage = (int)(this.maxDamagePercent * (float)this.returnStack.getMaxDamage());
            int itemDamage = itemStack.getMaxDamage() - random.nextInt(random.nextInt(maxDamage) + 1);

            if (itemDamage > maxDamage)
            {
                itemDamage = maxDamage;
            }

            if (itemDamage < 1)
            {
                itemDamage = 1;
            }

            itemStack.setItemDamage(itemDamage);
        }

        if (this.enchantable)
        {
            EnchantmentHelper.addRandomEnchantment(random, itemStack, 30);
        }

        return itemStack;
    }

    public WeightedRandomFishable setMaxDamagePercent(float maxDamagePercentIn)
    {
        this.maxDamagePercent = maxDamagePercentIn;
        return this;
    }

    public WeightedRandomFishable setEnchantable()
    {
        this.enchantable = true;
        return this;
    }
}
