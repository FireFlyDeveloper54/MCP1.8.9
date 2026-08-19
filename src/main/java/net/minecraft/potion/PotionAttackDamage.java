package net.minecraft.potion;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.ResourceLocation;

public class PotionAttackDamage extends Potion
{
    protected PotionAttackDamage(int potionID, ResourceLocation location, boolean badEffect, int potionColor)
    {
        super(potionID, location, badEffect, potionColor);
    }

    public double getAttributeModifierAmount(int amplifier, AttributeModifier modifier)
    {
        return this.id == Potion.weakness.id ? (double)(-0.5F * (float)(amplifier + 1)) : 1.3D * (double)(amplifier + 1);
    }
}
