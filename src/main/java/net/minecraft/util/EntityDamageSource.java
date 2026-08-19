package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class EntityDamageSource extends DamageSource
{
    protected Entity damageSourceEntity;
    private boolean isThornsDamage = false;

    public EntityDamageSource(String damageTypeIn, Entity damageSourceEntityIn)
    {
        super(damageTypeIn);
        this.damageSourceEntity = damageSourceEntityIn;
    }

    public EntityDamageSource setIsThornsDamage()
    {
        this.isThornsDamage = true;
        return this;
    }

    public boolean getIsThornsDamage()
    {
        return this.isThornsDamage;
    }

    public Entity getEntity()
    {
        return this.damageSourceEntity;
    }

    public IChatComponent getDeathMessage(EntityLivingBase entityLivingBaseIn)
    {
        ItemStack heldItem = this.damageSourceEntity instanceof EntityLivingBase ? ((EntityLivingBase)this.damageSourceEntity).getHeldItem() : null;
        String translationKey = "death.attack." + this.damageType;
        String itemTranslationKey = translationKey + ".item";
        return heldItem != null && heldItem.hasDisplayName() && StatCollector.canTranslate(itemTranslationKey) ? new ChatComponentTranslation(itemTranslationKey, new Object[] {entityLivingBaseIn.getDisplayName(), this.damageSourceEntity.getDisplayName(), heldItem.getChatComponent()}): new ChatComponentTranslation(translationKey, new Object[] {entityLivingBaseIn.getDisplayName(), this.damageSourceEntity.getDisplayName()});
    }

    public boolean isDifficultyScaled()
    {
        return this.damageSourceEntity != null && this.damageSourceEntity instanceof EntityLivingBase && !(this.damageSourceEntity instanceof EntityPlayer);
    }
}
