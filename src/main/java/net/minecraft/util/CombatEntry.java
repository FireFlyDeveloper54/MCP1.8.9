package net.minecraft.util;

import net.minecraft.entity.EntityLivingBase;

public class CombatEntry
{
    private final DamageSource damageSrc;
    private final int tick;
    private final float damage;
    private final float health;
    private final String environment;
    private final float fallDistance;

    public CombatEntry(DamageSource damageSrcIn, int tickIn, float healthAmount, float damageAmount, String environmentIn, float fallDistanceIn)
    {
        this.damageSrc = damageSrcIn;
        this.tick = tickIn;
        this.damage = damageAmount;
        this.health = healthAmount;
        this.environment = environmentIn;
        this.fallDistance = fallDistanceIn;
    }

    public DamageSource getDamageSrc()
    {
        return this.damageSrc;
    }

    public float getDamageAmountInternal()
    {
        return this.damage;
    }

    public boolean isLivingDamageSrc()
    {
        return this.damageSrc.getEntity() instanceof EntityLivingBase;
    }

    public String getEnvironment()
    {
        return this.environment;
    }

    public IChatComponent getDamageSrcDisplayName()
    {
        return this.getDamageSrc().getEntity() == null ? null : this.getDamageSrc().getEntity().getDisplayName();
    }

    public float getDamageAmount()
    {
        return this.damageSrc == DamageSource.outOfWorld ? Float.MAX_VALUE : this.fallDistance;
    }
}
