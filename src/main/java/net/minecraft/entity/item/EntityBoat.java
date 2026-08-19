package net.minecraft.entity.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityBoat extends Entity
{
    private boolean isBoatEmpty;
    private double speedMultiplier;
    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private final BlockPos.MutableBlockPos blockSamplePos = new BlockPos.MutableBlockPos();

    public EntityBoat(World worldIn)
    {
        super(worldIn);
        this.isBoatEmpty = true;
        this.speedMultiplier = 0.07D;
        this.preventEntitySpawning = true;
        this.setSize(1.5F, 0.6F);
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    protected void entityInit()
    {
        this.dataWatcher.addObject(17, Integer.valueOf(0));
        this.dataWatcher.addObject(18, Integer.valueOf(1));
        this.dataWatcher.addObject(19, Float.valueOf(0.0F));
    }

    public AxisAlignedBB getCollisionBox(Entity entityIn)
    {
        return entityIn.getEntityBoundingBox();
    }

    public AxisAlignedBB getCollisionBoundingBox()
    {
        return this.getEntityBoundingBox();
    }

    public boolean canBePushed()
    {
        return true;
    }

    public EntityBoat(World worldIn, double x, double y, double z)
    {
        this(worldIn);
        this.setPosition(x, y, z);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    public double getMountedYOffset()
    {
        return -0.3D;
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (!this.worldObj.isRemote && !this.isDead)
        {
            if (this.riddenByEntity != null && this.riddenByEntity == source.getEntity() && source instanceof EntityDamageSourceIndirect)
            {
                return false;
            }
            else
            {
                this.setForwardDirection(-this.getForwardDirection());
                this.setTimeSinceHit(10);
                this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
                this.setBeenAttacked();
                boolean attackerIsCreativePlayer = source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;

                if (attackerIsCreativePlayer || this.getDamageTaken() > 40.0F)
                {
                    if (this.riddenByEntity != null)
                    {
                        this.riddenByEntity.mountEntity(this);
                    }

                    if (!attackerIsCreativePlayer && this.worldObj.getGameRules().getBoolean("doEntityDrops"))
                    {
                        this.dropItemWithOffset(Items.boat, 1, 0.0F);
                    }

                    this.setDead();
                }

                return true;
            }
        }
        else
        {
            return true;
        }
    }

    public void performHurtAnimation()
    {
        this.setForwardDirection(-this.getForwardDirection());
        this.setTimeSinceHit(10);
        this.setDamageTaken(this.getDamageTaken() * 11.0F);
    }

    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        if (teleport && this.riddenByEntity != null)
        {
            this.prevPosX = this.posX = x;
            this.prevPosY = this.posY = y;
            this.prevPosZ = this.posZ = z;
            this.rotationYaw = yaw;
            this.rotationPitch = pitch;
            this.boatPosRotationIncrements = 0;
            this.setPosition(x, y, z);
            this.motionX = this.velocityX = 0.0D;
            this.motionY = this.velocityY = 0.0D;
            this.motionZ = this.velocityZ = 0.0D;
        }
        else
        {
            if (this.isBoatEmpty)
            {
                this.boatPosRotationIncrements = posRotationIncrements + 5;
            }
            else
            {
                double deltaX = x - this.posX;
                double deltaY = y - this.posY;
                double deltaZ = z - this.posZ;
                double deltaSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

                if (deltaSq <= 1.0D)
                {
                    return;
                }

                this.boatPosRotationIncrements = 3;
            }

            this.boatX = x;
            this.boatY = y;
            this.boatZ = z;
            this.boatYaw = (double)yaw;
            this.boatPitch = (double)pitch;
            this.motionX = this.velocityX;
            this.motionY = this.velocityY;
            this.motionZ = this.velocityZ;
        }
    }

    public void setVelocity(double x, double y, double z)
    {
        this.velocityX = this.motionX = x;
        this.velocityY = this.motionY = y;
        this.velocityZ = this.motionZ = z;
    }

    public void onUpdate()
    {
        super.onUpdate();

        if (this.getTimeSinceHit() > 0)
        {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        }

        if (this.getDamageTaken() > 0.0F)
        {
            this.setDamageTaken(this.getDamageTaken() - 1.0F);
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        int waterSliceCount = 5;
        double waterCoverage = 0.0D;

        for (int sliceIndex = 0; sliceIndex < waterSliceCount; ++sliceIndex)
        {
            double sliceMinY = this.getEntityBoundingBox().minY + (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) * (double)(sliceIndex + 0) / (double)waterSliceCount - 0.125D;
            double sliceMaxY = this.getEntityBoundingBox().minY + (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) * (double)(sliceIndex + 1) / (double)waterSliceCount - 0.125D;
            AxisAlignedBB axisAlignedBB = new AxisAlignedBB(this.getEntityBoundingBox().minX, sliceMinY, this.getEntityBoundingBox().minZ, this.getEntityBoundingBox().maxX, sliceMaxY, this.getEntityBoundingBox().maxZ);

            if (this.worldObj.isAABBInMaterial(axisAlignedBB, Material.water))
            {
                waterCoverage += 1.0D / (double)waterSliceCount;
            }
        }

        double horizontalSpeedBeforeUpdate = MathHelper.length_double(this.motionX, this.motionZ);

        if (horizontalSpeedBeforeUpdate > 0.2975D)
        {
            double yawCos = Math.cos((double)this.rotationYaw * Math.PI / 180.0D);
            double yawSin = Math.sin((double)this.rotationYaw * Math.PI / 180.0D);

            for (int splashIndex = 0; (double)splashIndex < 1.0D + horizontalSpeedBeforeUpdate * 60.0D; ++splashIndex)
            {
                double randomLateral = (double)(this.rand.nextFloat() * 2.0F - 1.0F);
                double sideOffset = (double)(this.rand.nextInt(2) * 2 - 1) * 0.7D;

                if (this.rand.nextBoolean())
                {
                    double splashX = this.posX - yawCos * randomLateral * 0.8D + yawSin * sideOffset;
                    double splashZ = this.posZ - yawSin * randomLateral * 0.8D - yawCos * sideOffset;
                    this.worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, splashX, this.posY - 0.125D, splashZ, this.motionX, this.motionY, this.motionZ, EnumParticleTypes.EMPTY_ARGS);
                }
                else
                {
                    double splashX = this.posX + yawCos + yawSin * randomLateral * 0.7D;
                    double splashZ = this.posZ + yawSin - yawCos * randomLateral * 0.7D;
                    this.worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, splashX, this.posY - 0.125D, splashZ, this.motionX, this.motionY, this.motionZ, EnumParticleTypes.EMPTY_ARGS);
                }
            }
        }

        if (this.worldObj.isRemote && this.isBoatEmpty)
        {
            if (this.boatPosRotationIncrements > 0)
            {
                double interpolatedX = this.posX + (this.boatX - this.posX) / (double)this.boatPosRotationIncrements;
                double interpolatedY = this.posY + (this.boatY - this.posY) / (double)this.boatPosRotationIncrements;
                double interpolatedZ = this.posZ + (this.boatZ - this.posZ) / (double)this.boatPosRotationIncrements;
                double yawDelta = MathHelper.wrapAngleTo180_double(this.boatYaw - (double)this.rotationYaw);
                this.rotationYaw = (float)((double)this.rotationYaw + yawDelta / (double)this.boatPosRotationIncrements);
                this.rotationPitch = (float)((double)this.rotationPitch + (this.boatPitch - (double)this.rotationPitch) / (double)this.boatPosRotationIncrements);
                --this.boatPosRotationIncrements;
                this.setPosition(interpolatedX, interpolatedY, interpolatedZ);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            }
            else
            {
                double nextX = this.posX + this.motionX;
                double nextY = this.posY + this.motionY;
                double nextZ = this.posZ + this.motionZ;
                this.setPosition(nextX, nextY, nextZ);

                if (this.onGround)
                {
                    this.motionX *= 0.5D;
                    this.motionY *= 0.5D;
                    this.motionZ *= 0.5D;
                }

                this.motionX *= 0.9900000095367432D;
                this.motionY *= 0.949999988079071D;
                this.motionZ *= 0.9900000095367432D;
            }
        }
        else
        {
            if (waterCoverage < 1.0D)
            {
                double buoyancyFactor = waterCoverage * 2.0D - 1.0D;
                this.motionY += 0.03999999910593033D * buoyancyFactor;
            }
            else
            {
                if (this.motionY < 0.0D)
                {
                    this.motionY /= 2.0D;
                }

                this.motionY += 0.007000000216066837D;
            }

            if (this.riddenByEntity instanceof EntityLivingBase)
            {
                EntityLivingBase entityLivingBase = (EntityLivingBase)this.riddenByEntity;
                float riderYaw = this.riddenByEntity.rotationYaw + -entityLivingBase.moveStrafing * 90.0F;
                this.motionX += -Math.sin((double)(riderYaw * (float)Math.PI / 180.0F)) * this.speedMultiplier * (double)entityLivingBase.moveForward * 0.05000000074505806D;
                this.motionZ += Math.cos((double)(riderYaw * (float)Math.PI / 180.0F)) * this.speedMultiplier * (double)entityLivingBase.moveForward * 0.05000000074505806D;
            }

            double horizontalSpeed = MathHelper.length_double(this.motionX, this.motionZ);

            if (horizontalSpeed > 0.35D)
            {
                double speedScale = 0.35D / horizontalSpeed;
                this.motionX *= speedScale;
                this.motionZ *= speedScale;
                horizontalSpeed = 0.35D;
            }

            if (horizontalSpeed > horizontalSpeedBeforeUpdate && this.speedMultiplier < 0.35D)
            {
                this.speedMultiplier += (0.35D - this.speedMultiplier) / 35.0D;

                if (this.speedMultiplier > 0.35D)
                {
                    this.speedMultiplier = 0.35D;
                }
            }
            else
            {
                this.speedMultiplier -= (this.speedMultiplier - 0.07D) / 35.0D;

                if (this.speedMultiplier < 0.07D)
                {
                    this.speedMultiplier = 0.07D;
                }
            }

            for (int cornerIndex = 0; cornerIndex < 4; ++cornerIndex)
            {
                int blockX = MathHelper.floor_double(this.posX + ((double)(cornerIndex % 2) - 0.5D) * 0.8D);
                int blockZ = MathHelper.floor_double(this.posZ + ((double)(cornerIndex / 2) - 0.5D) * 0.8D);

                for (int yOffsetIndex = 0; yOffsetIndex < 2; ++yOffsetIndex)
                {
                    int blockY = MathHelper.floor_double(this.posY) + yOffsetIndex;
                    BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);
                    Block block = this.worldObj.getBlockState(blockPos).getBlock();

                    if (block == Blocks.snow_layer)
                    {
                        this.worldObj.setBlockToAir(blockPos);
                        this.isCollidedHorizontally = false;
                    }
                    else if (block == Blocks.waterlily)
                    {
                        this.worldObj.destroyBlock(blockPos, true);
                        this.isCollidedHorizontally = false;
                    }
                }
            }

            if (this.onGround)
            {
                this.motionX *= 0.5D;
                this.motionY *= 0.5D;
                this.motionZ *= 0.5D;
            }

            this.moveEntity(this.motionX, this.motionY, this.motionZ);

            if (this.isCollidedHorizontally && horizontalSpeedBeforeUpdate > 0.2975D)
            {
                if (!this.worldObj.isRemote && !this.isDead)
                {
                    this.setDead();

                    if (this.worldObj.getGameRules().getBoolean("doEntityDrops"))
                    {
                        for (int plankDropIndex = 0; plankDropIndex < 3; ++plankDropIndex)
                        {
                            this.dropItemWithOffset(Item.getItemFromBlock(Blocks.planks), 1, 0.0F);
                        }

                        for (int stickDropIndex = 0; stickDropIndex < 2; ++stickDropIndex)
                        {
                            this.dropItemWithOffset(Items.stick, 1, 0.0F);
                        }
                    }
                }
            }
            else
            {
                this.motionX *= 0.9900000095367432D;
                this.motionY *= 0.949999988079071D;
                this.motionZ *= 0.9900000095367432D;
            }

            this.rotationPitch = 0.0F;
            double targetYaw = (double)this.rotationYaw;
            double movementDeltaX = this.prevPosX - this.posX;
            double movementDeltaZ = this.prevPosZ - this.posZ;

            if (movementDeltaX * movementDeltaX + movementDeltaZ * movementDeltaZ > 0.001D)
            {
                targetYaw = (double)((float)(MathHelper.atan2(movementDeltaZ, movementDeltaX) * 180.0D / Math.PI));
            }

            double yawChange = MathHelper.wrapAngleTo180_double(targetYaw - (double)this.rotationYaw);

            if (yawChange > 20.0D)
            {
                yawChange = 20.0D;
            }

            if (yawChange < -20.0D)
            {
                yawChange = -20.0D;
            }

            this.rotationYaw = (float)((double)this.rotationYaw + yawChange);
            this.setRotation(this.rotationYaw, this.rotationPitch);

            if (!this.worldObj.isRemote)
            {
                List<Entity> nearbyEntities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));

                if (nearbyEntities != null && !nearbyEntities.isEmpty())
                {
                    for (int entityIndex = 0; entityIndex < nearbyEntities.size(); ++entityIndex)
                    {
                        Entity entity = (Entity)nearbyEntities.get(entityIndex);

                        if (entity != this.riddenByEntity && entity.canBePushed() && entity instanceof EntityBoat)
                        {
                            entity.applyEntityCollision(this);
                        }
                    }
                }

                if (this.riddenByEntity != null && this.riddenByEntity.isDead)
                {
                    this.riddenByEntity = null;
                }
            }
        }
    }

    public void updateRiderPosition()
    {
        if (this.riddenByEntity != null)
        {
            double riderOffsetX = Math.cos((double)this.rotationYaw * Math.PI / 180.0D) * 0.4D;
            double riderOffsetZ = Math.sin((double)this.rotationYaw * Math.PI / 180.0D) * 0.4D;
            this.riddenByEntity.setPosition(this.posX + riderOffsetX, this.posY + this.getMountedYOffset() + this.riddenByEntity.getYOffset(), this.posZ + riderOffsetZ);
        }
    }

    protected void writeEntityToNBT(NBTTagCompound tagCompound)
    {
    }

    protected void readEntityFromNBT(NBTTagCompound tagCompund)
    {
    }

    public boolean interactFirst(EntityPlayer playerIn)
    {
        if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer && this.riddenByEntity != playerIn)
        {
            return true;
        }
        else
        {
            if (!this.worldObj.isRemote)
            {
                playerIn.mountEntity(this);
            }

            return true;
        }
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos)
    {
        if (onGroundIn)
        {
            if (this.fallDistance > 3.0F)
            {
                this.fall(this.fallDistance, 1.0F);

                if (!this.worldObj.isRemote && !this.isDead)
                {
                    this.setDead();

                    if (this.worldObj.getGameRules().getBoolean("doEntityDrops"))
                    {
                        for (int plankDropIndex = 0; plankDropIndex < 3; ++plankDropIndex)
                        {
                            this.dropItemWithOffset(Item.getItemFromBlock(Blocks.planks), 1, 0.0F);
                        }

                        for (int stickDropIndex = 0; stickDropIndex < 2; ++stickDropIndex)
                        {
                            this.dropItemWithOffset(Items.stick, 1, 0.0F);
                        }
                    }
                }

                this.fallDistance = 0.0F;
            }
        }
        else if (this.worldObj.getBlockState(this.blockSamplePos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY) - 1, MathHelper.floor_double(this.posZ))).getBlock().getMaterial() != Material.water && y < 0.0D)
        {
            this.fallDistance = (float)((double)this.fallDistance - y);
        }
    }

    public void setDamageTaken(float damageTaken)
    {
        this.dataWatcher.updateObject(19, Float.valueOf(damageTaken));
    }

    public float getDamageTaken()
    {
        return this.dataWatcher.getWatchableObjectFloat(19);
    }

    public void setTimeSinceHit(int timeSinceHit)
    {
        this.dataWatcher.updateObject(17, Integer.valueOf(timeSinceHit));
    }

    public int getTimeSinceHit()
    {
        return this.dataWatcher.getWatchableObjectInt(17);
    }

    public void setForwardDirection(int forwardDirection)
    {
        this.dataWatcher.updateObject(18, Integer.valueOf(forwardDirection));
    }

    public int getForwardDirection()
    {
        return this.dataWatcher.getWatchableObjectInt(18);
    }

    public void setIsBoatEmpty(boolean isBoatEmptyIn)
    {
        this.isBoatEmpty = isBoatEmptyIn;
    }
}
