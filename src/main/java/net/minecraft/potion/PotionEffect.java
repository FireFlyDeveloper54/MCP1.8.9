package net.minecraft.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PotionEffect
{
    private static final Logger LOGGER = LogManager.getLogger();
    private int potionID;
    private int duration;
    private int amplifier;
    private boolean isSplashPotion;
    private boolean isAmbient;
    private boolean isPotionDurationMax;
    private boolean showParticles;

    public PotionEffect(int id, int effectDuration)
    {
        this(id, effectDuration, 0);
    }

    public PotionEffect(int id, int effectDuration, int effectAmplifier)
    {
        this(id, effectDuration, effectAmplifier, false, true);
    }

    public PotionEffect(int id, int effectDuration, int effectAmplifier, boolean ambient, boolean showParticles)
    {
        this.potionID = id;
        this.duration = effectDuration;
        this.amplifier = effectAmplifier;
        this.isAmbient = ambient;
        this.showParticles = showParticles;
    }

    public PotionEffect(PotionEffect other)
    {
        this.potionID = other.potionID;
        this.duration = other.duration;
        this.amplifier = other.amplifier;
        this.isAmbient = other.isAmbient;
        this.showParticles = other.showParticles;
    }

    public void combine(PotionEffect other)
    {
        if (this.potionID != other.potionID)
        {
            LOGGER.warn("This method should only be called for matching effects!");
        }

        if (other.amplifier > this.amplifier)
        {
            this.amplifier = other.amplifier;
            this.duration = other.duration;
        }
        else if (other.amplifier == this.amplifier && this.duration < other.duration)
        {
            this.duration = other.duration;
        }
        else if (!other.isAmbient && this.isAmbient)
        {
            this.isAmbient = other.isAmbient;
        }

        this.showParticles = other.showParticles;
    }

    public int getPotionID()
    {
        return this.potionID;
    }

    public int getDuration()
    {
        return this.duration;
    }

    public int getAmplifier()
    {
        return this.amplifier;
    }

    public void setSplashPotion(boolean splashPotion)
    {
        this.isSplashPotion = splashPotion;
    }

    public boolean getIsAmbient()
    {
        return this.isAmbient;
    }

    public boolean getIsShowParticles()
    {
        return this.showParticles;
    }

    public boolean onUpdate(EntityLivingBase entityIn)
    {
        if (this.duration > 0)
        {
            if (Potion.potionTypes[this.potionID].isReady(this.duration, this.amplifier))
            {
                this.performEffect(entityIn);
            }

            this.deincrementDuration();
        }

        return this.duration > 0;
    }

    private int deincrementDuration()
    {
        return --this.duration;
    }

    public void performEffect(EntityLivingBase entityIn)
    {
        if (this.duration > 0)
        {
            Potion.potionTypes[this.potionID].performEffect(entityIn, this.amplifier);
        }
    }

    public String getEffectName()
    {
        return Potion.potionTypes[this.potionID].getName();
    }

    public int hashCode()
    {
        return this.potionID;
    }

    public String toString()
    {
        String description = "";

        if (this.getAmplifier() > 0)
        {
            description = this.getEffectName() + " x " + (this.getAmplifier() + 1) + ", Duration: " + this.getDuration();
        }
        else
        {
            description = this.getEffectName() + ", Duration: " + this.getDuration();
        }

        if (this.isSplashPotion)
        {
            description = description + ", Splash: true";
        }

        if (!this.showParticles)
        {
            description = description + ", Particles: false";
        }

        return Potion.potionTypes[this.potionID].isUsable() ? "(" + description + ")" : description;
    }

    public boolean equals(Object other)
    {
        if (!(other instanceof PotionEffect))
        {
            return false;
        }
        else
        {
            PotionEffect potionEffect = (PotionEffect)other;
            return this.potionID == potionEffect.potionID && this.amplifier == potionEffect.amplifier && this.duration == potionEffect.duration && this.isSplashPotion == potionEffect.isSplashPotion && this.isAmbient == potionEffect.isAmbient;
        }
    }

    public NBTTagCompound writeCustomPotionEffectToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("Id", (byte)this.getPotionID());
        nbt.setByte("Amplifier", (byte)this.getAmplifier());
        nbt.setInteger("Duration", this.getDuration());
        nbt.setBoolean("Ambient", this.getIsAmbient());
        nbt.setBoolean("ShowParticles", this.getIsShowParticles());
        return nbt;
    }

    public static PotionEffect readCustomPotionEffectFromNBT(NBTTagCompound nbt)
    {
        int potionId = nbt.getByte("Id");

        if (potionId >= 0 && potionId < Potion.potionTypes.length && Potion.potionTypes[potionId] != null)
        {
            int amplifier = nbt.getByte("Amplifier");
            int duration = nbt.getInteger("Duration");
            boolean ambient = nbt.getBoolean("Ambient");
            boolean showParticles = true;

            if (nbt.hasKey("ShowParticles", 1))
            {
                showParticles = nbt.getBoolean("ShowParticles");
            }

            return new PotionEffect(potionId, duration, amplifier, ambient, showParticles);
        }
        else
        {
            return null;
        }
    }

    public void setPotionDurationMax(boolean maxDuration)
    {
        this.isPotionDurationMax = maxDuration;
    }

    public boolean getIsPotionDurationMax()
    {
        return this.isPotionDurationMax;
    }
}
