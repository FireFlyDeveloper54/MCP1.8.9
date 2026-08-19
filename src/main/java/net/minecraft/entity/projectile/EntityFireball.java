package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntityFireball extends Entity
{
    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private Block inTile;
    private boolean inGround;
    public EntityLivingBase shootingEntity;
    private int ticksAlive;
    private int ticksInAir;
    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;
    private final BlockPos.MutableBlockPos blockSamplePos = new BlockPos.MutableBlockPos();

    public EntityFireball(World worldIn)
    {
        super(worldIn);
        this.setSize(1.0F, 1.0F);
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

    public EntityFireball(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ)
    {
        super(worldIn);
        this.setSize(1.0F, 1.0F);
        this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
        this.setPosition(x, y, z);
        double doubleValue = (double)MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
        this.accelerationX = accelX / doubleValue * 0.1D;
        this.accelerationY = accelY / doubleValue * 0.1D;
        this.accelerationZ = accelZ / doubleValue * 0.1D;
    }

    public EntityFireball(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ)
    {
        super(worldIn);
        this.shootingEntity = shooter;
        this.setSize(1.0F, 1.0F);
        this.setLocationAndAngles(shooter.posX, shooter.posY, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionX = this.motionY = this.motionZ = 0.0D;
        accelX = accelX + this.rand.nextGaussian() * 0.4D;
        accelY = accelY + this.rand.nextGaussian() * 0.4D;
        accelZ = accelZ + this.rand.nextGaussian() * 0.4D;
        double doubleValue = (double)MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
        this.accelerationX = accelX / doubleValue * 0.1D;
        this.accelerationY = accelY / doubleValue * 0.1D;
        this.accelerationZ = accelZ / doubleValue * 0.1D;
    }

    public void onUpdate()
    {
        if (this.worldObj.isRemote || (this.shootingEntity == null || !this.shootingEntity.isDead) && this.isCurrentBlockLoaded())
        {
            super.onUpdate();
            this.setFire(1);

            if (this.inGround)
            {
                if (this.worldObj.getBlockState(this.blockSamplePos.set(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile)
                {
                    ++this.ticksAlive;

                    if (this.ticksAlive == 600)
                    {
                        this.setDead();
                    }

                    return;
                }

                this.inGround = false;
                this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
                this.ticksAlive = 0;
                this.ticksInAir = 0;
            }
            else
            {
                ++this.ticksInAir;
            }

            Vec3 currentPosition = new Vec3(this.posX, this.posY, this.posZ);
            Vec3 nextPosition = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingObjectPosition = this.worldObj.rayTraceBlocks(currentPosition, nextPosition);

            if (movingObjectPosition != null)
            {
                nextPosition = new Vec3(movingObjectPosition.hitVec.xCoord, movingObjectPosition.hitVec.yCoord, movingObjectPosition.hitVec.zCoord);
            }

            Entity entity = null;
            List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double doubleValue = 0.0D;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity1 = (Entity)list.get(i);

                if (entity1.canBeCollidedWith() && (!entity1.isEntityEqual(this.shootingEntity) || this.ticksInAir >= 25))
                {
                    float f = 0.3F;
                    AxisAlignedBB axisAlignedBB = entity1.getEntityBoundingBox().expand((double)f, (double)f, (double)f);
                    MovingObjectPosition movingobjectposition1 = axisAlignedBB.calculateIntercept(currentPosition, nextPosition);

                    if (movingobjectposition1 != null)
                    {
                        double doubleValue2 = currentPosition.squareDistanceTo(movingobjectposition1.hitVec);

                        if (doubleValue2 < doubleValue || doubleValue == 0.0D)
                        {
                            entity = entity1;
                            doubleValue = doubleValue2;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingObjectPosition = new MovingObjectPosition(entity);
            }

            if (movingObjectPosition != null)
            {
                this.onImpact(movingObjectPosition);
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            float floatValue2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(MathHelper.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;

            for (this.rotationPitch = (float)(MathHelper.atan2((double)floatValue2, this.motionY) * 180.0D / Math.PI) - 90.0F; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
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
            float floatValue3 = this.getMotionFactor();

            if (this.isInWater())
            {
                for (int j = 0; j < 4; ++j)
                {
                    float floatValue = 0.25F;
                    this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double)floatValue, this.posY - this.motionY * (double)floatValue, this.posZ - this.motionZ * (double)floatValue, this.motionX, this.motionY, this.motionZ, EnumParticleTypes.EMPTY_ARGS);
                }

                floatValue3 = 0.8F;
            }

            this.motionX += this.accelerationX;
            this.motionY += this.accelerationY;
            this.motionZ += this.accelerationZ;
            this.motionX *= (double)floatValue3;
            this.motionY *= (double)floatValue3;
            this.motionZ *= (double)floatValue3;
            this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
            this.setPosition(this.posX, this.posY, this.posZ);
        }
        else
        {
            this.setDead();
        }
    }

    private boolean isCurrentBlockLoaded()
    {
        return this.worldObj.isBlockLoaded(this.blockSamplePos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)));
    }

    protected float getMotionFactor()
    {
        return 0.95F;
    }

    protected abstract void onImpact(MovingObjectPosition movingObject);

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setShort("xTile", (short)this.xTile);
        tagCompound.setShort("yTile", (short)this.yTile);
        tagCompound.setShort("zTile", (short)this.zTile);
        ResourceLocation resourceLocation = (ResourceLocation)Block.blockRegistry.getNameForObject(this.inTile);
        tagCompound.setString("inTile", resourceLocation == null ? "" : resourceLocation.toString());
        tagCompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
        tagCompound.setTag("direction", this.newDoubleNBTList(new double[] {this.motionX, this.motionY, this.motionZ}));
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        this.xTile = tagCompund.getShort("xTile");
        this.yTile = tagCompund.getShort("yTile");
        this.zTile = tagCompund.getShort("zTile");

        if (tagCompund.hasKey("inTile", 8))
        {
            this.inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
        }
        else
        {
            this.inTile = Block.getBlockById(tagCompund.getByte("inTile") & 255);
        }

        this.inGround = tagCompund.getByte("inGround") == 1;

        if (tagCompund.hasKey("direction", 9))
        {
            NBTTagList nBTTagList = tagCompund.getTagList("direction", 6);
            this.motionX = nBTTagList.getDoubleAt(0);
            this.motionY = nBTTagList.getDoubleAt(1);
            this.motionZ = nBTTagList.getDoubleAt(2);
        }
        else
        {
            this.setDead();
        }
    }

    public boolean canBeCollidedWith()
    {
        return true;
    }

    public float getCollisionBorderSize()
    {
        return 1.0F;
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            this.setBeenAttacked();

            if (source.getEntity() != null)
            {
                Vec3 lookVector = source.getEntity().getLookVec();

                if (lookVector != null)
                {
                    this.motionX = lookVector.xCoord;
                    this.motionY = lookVector.yCoord;
                    this.motionZ = lookVector.zCoord;
                    this.accelerationX = this.motionX * 0.1D;
                    this.accelerationY = this.motionY * 0.1D;
                    this.accelerationZ = this.motionZ * 0.1D;
                }

                if (source.getEntity() instanceof EntityLivingBase)
                {
                    this.shootingEntity = (EntityLivingBase)source.getEntity();
                }

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public float getBrightness(float partialTicks)
    {
        return 1.0F;
    }

    public int getBrightnessForRender(float partialTicks)
    {
        return 15728880;
    }
}
