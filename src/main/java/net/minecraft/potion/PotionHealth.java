package net.minecraft.potion;

import net.minecraft.util.ResourceLocation;

public class PotionHealth extends Potion
{
    public PotionHealth(int potionID, ResourceLocation location, boolean badEffect, int potionColor)
    {
        super(potionID, location, badEffect, potionColor);
    }

    public boolean isInstant()
    {
        return true;
    }

    public boolean isReady(int duration, int amplifier)
    {
        return duration >= 1;
    }
}
