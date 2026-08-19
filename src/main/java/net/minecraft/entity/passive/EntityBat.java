package net.minecraft.entity.passive;

import java.util.Calendar;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityBat extends EntityAmbientCreature
{
    private final BlockPos.MutableBlockPos spawnCheckPos = new BlockPos.MutableBlockPos();

    private BlockPos spawnPosition;

    public EntityBat(World worldIn)
    {
        super(worldIn);
        this.setSize(0.5F, 0.9F);
        this.setIsBatHanging(true);
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Byte.valueOf((byte)0));
    }

    protected float getSoundVolume()
    {
        return 0.1F;
    }

    protected float getSoundPitch()
    {
        return super.getSoundPitch() * 0.95F;
    }

    protected String getLivingSound()
    {
        return this.getIsBatHanging() && this.rand.nextInt(4) != 0 ? null : "mob.bat.idle";
    }

    protected String getHurtSound()
    {
        return "mob.bat.hurt";
    }

    protected String getDeathSound()
    {
        return "mob.bat.death";
    }

    public boolean canBePushed()
    {
        return false;
    }

    protected void collideWithEntity(Entity entityIn)
    {
    }

    protected void collideWithNearbyEntities()
    {
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(6.0D);
    }

    public boolean getIsBatHanging()
    {
        return (this.dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    public void setIsBatHanging(boolean isHanging)
    {
        byte byteValue = this.dataWatcher.getWatchableObjectByte(16);

        if (isHanging)
        {
            this.dataWatcher.updateObject(16, Byte.valueOf((byte)(byteValue | 1)));
        }
        else
        {
            this.dataWatcher.updateObject(16, Byte.valueOf((byte)(byteValue & -2)));
        }
    }

    public void onUpdate()
    {
        super.onUpdate();

        if (this.getIsBatHanging())
        {
            this.motionX = this.motionY = this.motionZ = 0.0D;
            this.posY = (double)MathHelper.floor_double(this.posY) + 1.0D - (double)this.height;
        }
        else
        {
            this.motionY *= 0.6000000238418579D;
        }
    }

    protected void updateAITasks()
    {
        super.updateAITasks();
        BlockPos blockPos = new BlockPos(this);
        BlockPos blockpos1 = blockPos.up();

        if (this.getIsBatHanging())
        {
            if (!this.worldObj.getBlockState(blockpos1).getBlock().isNormalCube())
            {
                this.setIsBatHanging(false);
                this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1015, blockPos, 0);
            }
            else
            {
                if (this.rand.nextInt(200) == 0)
                {
                    this.rotationYawHead = (float)this.rand.nextInt(360);
                }

                if (this.worldObj.getClosestPlayerToEntity(this, 4.0D) != null)
                {
                    this.setIsBatHanging(false);
                    this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1015, blockPos, 0);
                }
            }
        }
        else
        {
            if (this.spawnPosition != null && (!this.worldObj.isAirBlock(this.spawnPosition) || this.spawnPosition.getY() < 1))
            {
                this.spawnPosition = null;
            }

            if (this.spawnPosition == null || this.rand.nextInt(30) == 0 || this.spawnPosition.distanceSq((double)((int)this.posX), (double)((int)this.posY), (double)((int)this.posZ)) < 4.0D)
            {
                this.spawnPosition = new BlockPos((int)this.posX + this.rand.nextInt(7) - this.rand.nextInt(7), (int)this.posY + this.rand.nextInt(6) - 2, (int)this.posZ + this.rand.nextInt(7) - this.rand.nextInt(7));
            }

            double doubleValue = (double)this.spawnPosition.getX() + 0.5D - this.posX;
            double yCoordinate = (double)this.spawnPosition.getY() + 0.1D - this.posY;
            double zCoordinate = (double)this.spawnPosition.getZ() + 0.5D - this.posZ;
            this.motionX += (Math.signum(doubleValue) * 0.5D - this.motionX) * 0.10000000149011612D;
            this.motionY += (Math.signum(yCoordinate) * 0.699999988079071D - this.motionY) * 0.10000000149011612D;
            this.motionZ += (Math.signum(zCoordinate) * 0.5D - this.motionZ) * 0.10000000149011612D;
            float f = (float)(MathHelper.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) - 90.0F;
            float floatValue2 = MathHelper.wrapAngleTo180_float(f - this.rotationYaw);
            this.moveForward = 0.5F;
            this.rotationYaw += floatValue2;

            if (this.rand.nextInt(100) == 0 && this.worldObj.getBlockState(blockpos1).getBlock().isNormalCube())
            {
                this.setIsBatHanging(true);
            }
        }
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    public void fall(float distance, float damageMultiplier)
    {
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos)
    {
    }

    public boolean doesEntityNotTriggerPressurePlate()
    {
        return true;
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            if (!this.worldObj.isRemote && this.getIsBatHanging())
            {
                this.setIsBatHanging(false);
            }

            return super.attackEntityFrom(source, amount);
        }
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);
        this.dataWatcher.updateObject(16, Byte.valueOf(tagCompund.getByte("BatFlags")));
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setByte("BatFlags", this.dataWatcher.getWatchableObjectByte(16));
    }

    public boolean getCanSpawnHere()
    {
        BlockPos.MutableBlockPos blockPos = this.spawnCheckPos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.getEntityBoundingBox().minY), MathHelper.floor_double(this.posZ));

        if (blockPos.getY() >= this.worldObj.getSeaLevel())
        {
            return false;
        }
        else
        {
            int i = this.worldObj.getLightFromNeighbors(blockPos);
            int j = 4;

            if (this.isDateAroundHalloween(this.worldObj.getCurrentDate()))
            {
                j = 7;
            }
            else if (this.rand.nextBoolean())
            {
                return false;
            }

            return i > this.rand.nextInt(j) ? false : super.getCanSpawnHere();
        }
    }

    private boolean isDateAroundHalloween(Calendar calendar)
    {
        return calendar.get(2) + 1 == 10 && calendar.get(5) >= 20 || calendar.get(2) + 1 == 11 && calendar.get(5) <= 3;
    }

    public float getEyeHeight()
    {
        return this.height / 2.0F;
    }
}
