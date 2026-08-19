package net.minecraft.entity;

import net.minecraft.util.MathHelper;

public class EntityBodyHelper
{
    private EntityLivingBase theLiving;
    private int rotationTickCounter;
    private float prevRenderYawHead;

    public EntityBodyHelper(EntityLivingBase living)
    {
        this.theLiving = living;
    }

    public void updateRenderAngles()
    {
        double deltaX = this.theLiving.posX - this.theLiving.prevPosX;
        double deltaZ = this.theLiving.posZ - this.theLiving.prevPosZ;

        if (deltaX * deltaX + deltaZ * deltaZ > 2.500000277905201E-7D)
        {
            this.theLiving.renderYawOffset = this.theLiving.rotationYaw;
            this.theLiving.rotationYawHead = this.computeAngleWithBound(this.theLiving.renderYawOffset, this.theLiving.rotationYawHead, 75.0F);
            this.prevRenderYawHead = this.theLiving.rotationYawHead;
            this.rotationTickCounter = 0;
        }
        else
        {
            float f = 75.0F;

            if (Math.abs(this.theLiving.rotationYawHead - this.prevRenderYawHead) > 15.0F)
            {
                this.rotationTickCounter = 0;
                this.prevRenderYawHead = this.theLiving.rotationYawHead;
            }
            else
            {
                ++this.rotationTickCounter;
                int i = 10;

                if (this.rotationTickCounter > 10)
                {
                    f = Math.max(1.0F - (float)(this.rotationTickCounter - 10) / 10.0F, 0.0F) * 75.0F;
                }
            }

            this.theLiving.renderYawOffset = this.computeAngleWithBound(this.theLiving.rotationYawHead, this.theLiving.renderYawOffset, f);
        }
    }

    private float computeAngleWithBound(float targetAngle, float currentAngle, float maxDifference)
    {
        float f = MathHelper.wrapAngleTo180_float(targetAngle - currentAngle);

        if (f < -maxDifference)
        {
            f = -maxDifference;
        }

        if (f >= maxDifference)
        {
            f = maxDifference;
        }

        return targetAngle - f;
    }
}
