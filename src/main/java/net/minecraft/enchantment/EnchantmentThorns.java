package net.minecraft.enchantment;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class EnchantmentThorns extends Enchantment
{
    public EnchantmentThorns(int enchID, ResourceLocation enchName, int enchWeight)
    {
        super(enchID, enchName, enchWeight, EnumEnchantmentType.ARMOR_TORSO);
        this.setName("thorns");
    }

    public int getMinEnchantability(int enchantmentLevel)
    {
        return 10 + 20 * (enchantmentLevel - 1);
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
        return stack.getItem() instanceof ItemArmor ? true : super.canApply(stack);
    }

    public void onUserHurt(EntityLivingBase user, Entity attacker, int level)
    {
        Random random = user.getRNG();
        ItemStack itemStack = EnchantmentHelper.getEnchantedItem(Enchantment.thorns, user);

        if (shouldHit(level, random))
        {
            if (attacker != null)
            {
                attacker.attackEntityFrom(DamageSource.causeThornsDamage(user), (float)getDamage(level, random));
                attacker.playSound("damage.thorns", 0.5F, 1.0F);
            }

            if (itemStack != null)
            {
                itemStack.damageItem(3, user);
            }
        }
        else if (itemStack != null)
        {
            itemStack.damageItem(1, user);
        }
    }

    public static boolean shouldHit(int level, Random random)
    {
        return level <= 0 ? false : random.nextFloat() < 0.15F * (float)level;
    }

    public static int getDamage(int level, Random random)
    {
        return level > 10 ? level - 10 : 1 + random.nextInt(4);
    }
}
