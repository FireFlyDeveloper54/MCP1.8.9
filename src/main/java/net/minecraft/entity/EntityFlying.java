package net.minecraft.entity;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityFlying extends EntityLiving
{
    private final BlockPos.MutableBlockPos groundSamplePos = new BlockPos.MutableBlockPos();

    public EntityFlying(World worldIn)
    {
        super(worldIn);
    }

    public void fall(float distance, float damageMultiplier)
    {
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos)
    {
    }

    public void moveEntityWithHeading(float strafe, float forward)
    {
        if (this.isInWater())
        {
            this.moveFlying(strafe, forward, 0.02F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.800000011920929D;
            this.motionY *= 0.800000011920929D;
            this.motionZ *= 0.800000011920929D;
        }
        else if (this.isInLava())
        {
            this.moveFlying(strafe, forward, 0.02F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
        }
        else
        {
            float f = 0.91F;

            if (this.onGround)
            {
                f = this.worldObj.getBlockState(this.groundSamplePos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(this.posZ))).getBlock().slipperiness * 0.91F;
            }

            float floatValue = 0.16277136F / (f * f * f);
            this.moveFlying(strafe, forward, this.onGround ? 0.1F * floatValue : 0.02F);
            f = 0.91F;

            if (this.onGround)
            {
                f = this.worldObj.getBlockState(this.groundSamplePos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(this.posZ))).getBlock().slipperiness * 0.91F;
            }

            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= (double)f;
            this.motionY *= (double)f;
            this.motionZ *= (double)f;
        }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        double xCoordinate = this.posX - this.prevPosX;
        double zCoordinate = this.posZ - this.prevPosZ;
        float floatValue3 = MathHelper.sqrt_double(xCoordinate * xCoordinate + zCoordinate * zCoordinate) * 4.0F;

        if (floatValue3 > 1.0F)
        {
            floatValue3 = 1.0F;
        }

        this.limbSwingAmount += (floatValue3 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    public boolean isOnLadder()
    {
        return false;
    }
}
