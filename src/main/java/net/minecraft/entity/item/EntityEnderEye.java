package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityEnderEye extends Entity
{
    private double targetX;
    private double targetY;
    private double targetZ;
    private int despawnTimer;
    private boolean shatterOrDrop;

    public EntityEnderEye(World worldIn)
    {
        super(worldIn);
        this.setSize(0.25F, 0.25F);
    }

    protected void entityInit()
    {
    }

    public boolean isInRangeToRenderDist(double distance)
    {
        double doubleValue = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

        if (Double.isNaN(doubleValue))
        {
            doubleValue = 4.0D;
        }

        doubleValue = doubleValue * 64.0D;
        return distance < doubleValue * doubleValue;
    }

    public EntityEnderEye(World worldIn, double x, double y, double z)
    {
        super(worldIn);
        this.despawnTimer = 0;
        this.setSize(0.25F, 0.25F);
        this.setPosition(x, y, z);
    }

    public void moveTowards(BlockPos pos)
    {
        double xCoordinate = (double)pos.getX();
        int i = pos.getY();
        double zCoordinate = (double)pos.getZ();
        double xCoordinate2 = xCoordinate - this.posX;
        double zCoordinate2 = zCoordinate - this.posZ;
        float f = MathHelper.sqrt_double(xCoordinate2 * xCoordinate2 + zCoordinate2 * zCoordinate2);

        if (f > 12.0F)
        {
            this.targetX = this.posX + xCoordinate2 / (double)f * 12.0D;
            this.targetZ = this.posZ + zCoordinate2 / (double)f * 12.0D;
            this.targetY = this.posY + 8.0D;
        }
        else
        {
            this.targetX = xCoordinate;
            this.targetY = (double)i;
            this.targetZ = zCoordinate;
        }

        this.despawnTimer = 0;
        this.shatterOrDrop = this.rand.nextInt(5) > 0;
    }

    public void setVelocity(double x, double y, double z)
    {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(x * x + z * z);
            this.prevRotationYaw = this.rotationYaw = (float)(MathHelper.atan2(x, z) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float)(MathHelper.atan2(y, (double)f) * 180.0D / Math.PI);
        }
    }

    public void onUpdate()
    {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        super.onUpdate();
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

        if (!this.worldObj.isRemote)
        {
            double xCoordinate = this.targetX - this.posX;
            double zCoordinate = this.targetZ - this.posZ;
            float floatValue2 = (float)MathHelper.length_double(xCoordinate, zCoordinate);
            float floatValue3 = (float)MathHelper.atan2(zCoordinate, xCoordinate);
            double doubleValue = (double)f + (double)(floatValue2 - f) * 0.0025D;

            if (floatValue2 < 1.0F)
            {
                doubleValue *= 0.8D;
                this.motionY *= 0.8D;
            }

            this.motionX = Math.cos((double)floatValue3) * doubleValue;
            this.motionZ = Math.sin((double)floatValue3) * doubleValue;

            if (this.posY < this.targetY)
            {
                this.motionY += (1.0D - this.motionY) * 0.014999999664723873D;
            }
            else
            {
                this.motionY += (-1.0D - this.motionY) * 0.014999999664723873D;
            }
        }

        float floatValue4 = 0.25F;

        if (this.isInWater())
        {
            for (int i = 0; i < 4; ++i)
            {
                this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double)floatValue4, this.posY - this.motionY * (double)floatValue4, this.posZ - this.motionZ * (double)floatValue4, this.motionX, this.motionY, this.motionZ, EnumParticleTypes.EMPTY_ARGS);
            }
        }
        else
        {
            this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.posX - this.motionX * (double)floatValue4 + this.rand.nextDouble() * 0.6D - 0.3D, this.posY - this.motionY * (double)floatValue4 - 0.5D, this.posZ - this.motionZ * (double)floatValue4 + this.rand.nextDouble() * 0.6D - 0.3D, this.motionX, this.motionY, this.motionZ, EnumParticleTypes.EMPTY_ARGS);
        }

        if (!this.worldObj.isRemote)
        {
            this.setPosition(this.posX, this.posY, this.posZ);
            ++this.despawnTimer;

            if (this.despawnTimer > 80 && !this.worldObj.isRemote)
            {
                this.setDead();

                if (this.shatterOrDrop)
                {
                    this.worldObj.spawnEntityInWorld(new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, new ItemStack(Items.ender_eye)));
                }
                else
                {
                    this.worldObj.playAuxSFX(2003, new BlockPos(this), 0);
                }
            }
        }
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
    }

    public float getBrightness(float partialTicks)
    {
        return 1.0F;
    }

    public int getBrightnessForRender(float partialTicks)
    {
        return 15728880;
    }

    public boolean canAttackWithItem()
    {
        return false;
    }
}
