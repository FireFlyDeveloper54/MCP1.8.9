package net.minecraft.entity.player;

import net.minecraft.nbt.NBTTagCompound;

public class PlayerCapabilities
{
    public boolean disableDamage;
    public boolean isFlying;
    public boolean allowFlying;
    public boolean isCreativeMode;
    public boolean allowEdit = true;
    private float flySpeed = 0.05F;
    private float walkSpeed = 0.1F;

    public void writeCapabilitiesToNBT(NBTTagCompound tagCompound)
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        nBTTagCompound.setBoolean("invulnerable", this.disableDamage);
        nBTTagCompound.setBoolean("flying", this.isFlying);
        nBTTagCompound.setBoolean("mayfly", this.allowFlying);
        nBTTagCompound.setBoolean("instabuild", this.isCreativeMode);
        nBTTagCompound.setBoolean("mayBuild", this.allowEdit);
        nBTTagCompound.setFloat("flySpeed", this.flySpeed);
        nBTTagCompound.setFloat("walkSpeed", this.walkSpeed);
        tagCompound.setTag("abilities", nBTTagCompound);
    }

    public void readCapabilitiesFromNBT(NBTTagCompound tagCompound)
    {
        if (tagCompound.hasKey("abilities", 10))
        {
            NBTTagCompound nBTTagCompound = tagCompound.getCompoundTag("abilities");
            this.disableDamage = nBTTagCompound.getBoolean("invulnerable");
            this.isFlying = nBTTagCompound.getBoolean("flying");
            this.allowFlying = nBTTagCompound.getBoolean("mayfly");
            this.isCreativeMode = nBTTagCompound.getBoolean("instabuild");

            if (nBTTagCompound.hasKey("flySpeed", 99))
            {
                this.flySpeed = nBTTagCompound.getFloat("flySpeed");
                this.walkSpeed = nBTTagCompound.getFloat("walkSpeed");
            }

            if (nBTTagCompound.hasKey("mayBuild", 1))
            {
                this.allowEdit = nBTTagCompound.getBoolean("mayBuild");
            }
        }
    }

    public float getFlySpeed()
    {
        return this.flySpeed;
    }

    public void setFlySpeed(float speed)
    {
        this.flySpeed = speed;
    }

    public float getWalkSpeed()
    {
        return this.walkSpeed;
    }

    public void setPlayerWalkSpeed(float speed)
    {
        this.walkSpeed = speed;
    }
}
