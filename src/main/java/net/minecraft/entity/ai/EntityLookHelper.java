package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class EntityLookHelper
{
    private EntityLiving entity;
    private float deltaLookYaw;
    private float deltaLookPitch;
    private boolean isLooking;
    private double posX;
    private double posY;
    private double posZ;

    public EntityLookHelper(EntityLiving entitylivingIn)
    {
        this.entity = entitylivingIn;
    }

    public void setLookPositionWithEntity(Entity entityIn, float deltaYaw, float deltaPitch)
    {
        this.posX = entityIn.posX;

        if (entityIn instanceof EntityLivingBase)
        {
            this.posY = entityIn.posY + (double)entityIn.getEyeHeight();
        }
        else
        {
            this.posY = (entityIn.getEntityBoundingBox().minY + entityIn.getEntityBoundingBox().maxY) / 2.0D;
        }

        this.posZ = entityIn.posZ;
        this.deltaLookYaw = deltaYaw;
        this.deltaLookPitch = deltaPitch;
        this.isLooking = true;
    }

    public void setLookPosition(double x, double y, double z, float deltaYaw, float deltaPitch)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.deltaLookYaw = deltaYaw;
        this.deltaLookPitch = deltaPitch;
        this.isLooking = true;
    }

    public void onUpdateLook()
    {
        this.entity.rotationPitch = 0.0F;

        if (this.isLooking)
        {
            this.isLooking = false;
            double deltaX = this.posX - this.entity.posX;
            double deltaY = this.posY - (this.entity.posY + (double)this.entity.getEyeHeight());
            double deltaZ = this.posZ - this.entity.posZ;
            double horizontalDistance = (double)MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
            float targetYaw = (float)(MathHelper.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
            float targetPitch = (float)(-(MathHelper.atan2(deltaY, horizontalDistance) * 180.0D / Math.PI));
            this.entity.rotationPitch = this.updateRotation(this.entity.rotationPitch, targetPitch, this.deltaLookPitch);
            this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, targetYaw, this.deltaLookYaw);
        }
        else
        {
            this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, this.entity.renderYawOffset, 10.0F);
        }

        float headYawOffset = MathHelper.wrapAngleTo180_float(this.entity.rotationYawHead - this.entity.renderYawOffset);

        if (!this.entity.getNavigator().noPath())
        {
            if (headYawOffset < -75.0F)
            {
                this.entity.rotationYawHead = this.entity.renderYawOffset - 75.0F;
            }

            if (headYawOffset > 75.0F)
            {
                this.entity.rotationYawHead = this.entity.renderYawOffset + 75.0F;
            }
        }
    }

    private float updateRotation(float currentRotation, float targetRotation, float maxDelta)
    {
        float rotationDelta = MathHelper.wrapAngleTo180_float(targetRotation - currentRotation);

        if (rotationDelta > maxDelta)
        {
            rotationDelta = maxDelta;
        }

        if (rotationDelta < -maxDelta)
        {
            rotationDelta = -maxDelta;
        }

        return currentRotation + rotationDelta;
    }

    public boolean getIsLooking()
    {
        return this.isLooking;
    }

    public double getLookPosX()
    {
        return this.posX;
    }

    public double getLookPosY()
    {
        return this.posY;
    }

    public double getLookPosZ()
    {
        return this.posZ;
    }
}
