package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class EntityDamageSourceIndirect extends EntityDamageSource
{
    private Entity indirectEntity;

    public EntityDamageSourceIndirect(String damageTypeIn, Entity source, Entity indirectEntityIn)
    {
        super(damageTypeIn, source);
        this.indirectEntity = indirectEntityIn;
    }

    public Entity getSourceOfDamage()
    {
        return this.damageSourceEntity;
    }

    public Entity getEntity()
    {
        return this.indirectEntity;
    }

    public IChatComponent getDeathMessage(EntityLivingBase entityLivingBaseIn)
    {
        IChatComponent attackerDisplayName = this.indirectEntity == null ? this.damageSourceEntity.getDisplayName() : this.indirectEntity.getDisplayName();
        ItemStack heldItem = this.indirectEntity instanceof EntityLivingBase ? ((EntityLivingBase)this.indirectEntity).getHeldItem() : null;
        String translationKey = "death.attack." + this.damageType;
        String itemTranslationKey = translationKey + ".item";
        return heldItem != null && heldItem.hasDisplayName() && StatCollector.canTranslate(itemTranslationKey) ? new ChatComponentTranslation(itemTranslationKey, new Object[] {entityLivingBaseIn.getDisplayName(), attackerDisplayName, heldItem.getChatComponent()}): new ChatComponentTranslation(translationKey, new Object[] {entityLivingBaseIn.getDisplayName(), attackerDisplayName});
    }
}
