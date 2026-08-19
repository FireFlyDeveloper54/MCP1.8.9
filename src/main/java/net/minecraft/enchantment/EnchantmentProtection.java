package net.minecraft.enchantment;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class EnchantmentProtection extends Enchantment
{
    private static final String[] protectionName = new String[] {"all", "fire", "fall", "explosion", "projectile"};
    private static final int[] baseEnchantability = new int[] {1, 10, 5, 5, 3};
    private static final int[] levelEnchantability = new int[] {11, 8, 6, 8, 6};
    private static final int[] thresholdEnchantability = new int[] {20, 12, 10, 12, 15};
    public final int protectionType;

    public EnchantmentProtection(int enchID, ResourceLocation enchName, int enchWeight, int protectionTypeIn)
    {
        super(enchID, enchName, enchWeight, EnumEnchantmentType.ARMOR);
        this.protectionType = protectionTypeIn;

        if (protectionTypeIn == 2)
        {
            this.type = EnumEnchantmentType.ARMOR_FEET;
        }
    }

    public int getMinEnchantability(int enchantmentLevel)
    {
        return baseEnchantability[this.protectionType] + (enchantmentLevel - 1) * levelEnchantability[this.protectionType];
    }

    public int getMaxEnchantability(int enchantmentLevel)
    {
        return this.getMinEnchantability(enchantmentLevel) + thresholdEnchantability[this.protectionType];
    }

    public int getMaxLevel()
    {
        return 4;
    }

    public int calcModifierDamage(int level, DamageSource source)
    {
        if (source.canHarmInCreative())
        {
            return 0;
        }
        else
        {
            float f = (float)(6 + level * level) / 3.0F;
            return this.protectionType == 0 ? MathHelper.floor_float(f * 0.75F) : (this.protectionType == 1 && source.isFireDamage() ? MathHelper.floor_float(f * 1.25F) : (this.protectionType == 2 && source == DamageSource.fall ? MathHelper.floor_float(f * 2.5F) : (this.protectionType == 3 && source.isExplosion() ? MathHelper.floor_float(f * 1.5F) : (this.protectionType == 4 && source.isProjectile() ? MathHelper.floor_float(f * 1.5F) : 0))));
        }
    }

    public String getName()
    {
        return "enchantment.protect." + protectionName[this.protectionType];
    }

    public boolean canApplyTogether(Enchantment ench)
    {
        if (ench instanceof EnchantmentProtection)
        {
            EnchantmentProtection enchantmentProtection = (EnchantmentProtection)ench;
            return enchantmentProtection.protectionType == this.protectionType ? false : this.protectionType == 2 || enchantmentProtection.protectionType == 2;
        }
        else
        {
            return super.canApplyTogether(ench);
        }
    }

    public static int getFireTimeForEntity(Entity entity, int fireTicks)
    {
        int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantment.fireProtection.effectId, entity.getInventory());

        if (i > 0)
        {
            fireTicks -= MathHelper.floor_float((float)fireTicks * (float)i * 0.15F);
        }

        return fireTicks;
    }

    public static double getBlastDamageReduction(Entity entity, double damage)
    {
        int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantment.blastProtection.effectId, entity.getInventory());

        if (i > 0)
        {
            damage -= (double)MathHelper.floor_double(damage * (double)((float)i * 0.15F));
        }

        return damage;
    }
}
