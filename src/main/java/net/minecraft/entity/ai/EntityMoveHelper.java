package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.MathHelper;

public class EntityMoveHelper
{
    protected EntityLiving entity;
    protected double posX;
    protected double posY;
    protected double posZ;
    protected double speed;
    protected boolean update;

    public EntityMoveHelper(EntityLiving entitylivingIn)
    {
        this.entity = entitylivingIn;
        this.posX = entitylivingIn.posX;
        this.posY = entitylivingIn.posY;
        this.posZ = entitylivingIn.posZ;
    }

    public boolean isUpdating()
    {
        return this.update;
    }

    public double getSpeed()
    {
        return this.speed;
    }

    public void setMoveTo(double x, double y, double z, double speedIn)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = speedIn;
        this.update = true;
    }

    public void onUpdateMoveHelper()
    {
        this.entity.setMoveForward(0.0F);

        if (this.update)
        {
            this.update = false;
            int entityY = MathHelper.floor_double(this.entity.getEntityBoundingBox().minY + 0.5D);
            double deltaX = this.posX - this.entity.posX;
            double deltaZ = this.posZ - this.entity.posZ;
            double deltaY = this.posY - (double)entityY;
            double distanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

            if (distanceSq >= 2.500000277905201E-7D)
            {
                float targetYaw = (float)(MathHelper.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
                this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, targetYaw, 30.0F);
                this.entity.setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue()));

                if (deltaY > 0.0D && deltaX * deltaX + deltaZ * deltaZ < 1.0D)
                {
                    this.entity.getJumpHelper().setJumping();
                }
            }
        }
    }

    protected float limitAngle(float currentAngle, float targetAngle, float maxTurn)
    {
        float angleDelta = MathHelper.wrapAngleTo180_float(targetAngle - currentAngle);

        if (angleDelta > maxTurn)
        {
            angleDelta = maxTurn;
        }

        if (angleDelta < -maxTurn)
        {
            angleDelta = -maxTurn;
        }

        float limitedAngle = currentAngle + angleDelta;

        if (limitedAngle < 0.0F)
        {
            limitedAngle += 360.0F;
        }
        else if (limitedAngle > 360.0F)
        {
            limitedAngle -= 360.0F;
        }

        return limitedAngle;
    }

    public double getX()
    {
        return this.posX;
    }

    public double getY()
    {
        return this.posY;
    }

    public double getZ()
    {
        return this.posZ;
    }
}
