package net.minecraft.entity.monster;

import java.util.Random;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityGhast extends EntityFlying implements IMob
{
    private int explosionStrength = 1;

    public EntityGhast(World worldIn)
    {
        super(worldIn);
        this.setSize(4.0F, 4.0F);
        this.isImmuneToFire = true;
        this.experienceValue = 5;
        this.moveHelper = new EntityGhast.GhastMoveHelper(this);
        this.tasks.addTask(5, new EntityGhast.AIRandomFly(this));
        this.tasks.addTask(7, new EntityGhast.AILookAround(this));
        this.tasks.addTask(7, new EntityGhast.AIFireballAttack(this));
        this.targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
    }

    public boolean isAttacking()
    {
        return this.dataWatcher.getWatchableObjectByte(16) != 0;
    }

    public void setAttacking(boolean attacking)
    {
        this.dataWatcher.updateObject(16, Byte.valueOf((byte)(attacking ? 1 : 0)));
    }

    public int getFireballStrength()
    {
        return this.explosionStrength;
    }

    public void onUpdate()
    {
        super.onUpdate();

        if (!this.worldObj.isRemote && this.worldObj.getDifficulty() == EnumDifficulty.PEACEFUL)
        {
            this.setDead();
        }
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else if ("fireball".equals(source.getDamageType()) && source.getEntity() instanceof EntityPlayer)
        {
            super.attackEntityFrom(source, 1000.0F);
            ((EntityPlayer)source.getEntity()).triggerAchievement(AchievementList.ghast);
            return true;
        }
        else
        {
            return super.attackEntityFrom(source, amount);
        }
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Byte.valueOf((byte)0));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(100.0D);
    }

    protected String getLivingSound()
    {
        return "mob.ghast.moan";
    }

    protected String getHurtSound()
    {
        return "mob.ghast.scream";
    }

    protected String getDeathSound()
    {
        return "mob.ghast.death";
    }

    protected Item getDropItem()
    {
        return Items.gunpowder;
    }

    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier)
    {
        int i = this.rand.nextInt(2) + this.rand.nextInt(1 + lootingModifier);

        for (int j = 0; j < i; ++j)
        {
            this.dropItem(Items.ghast_tear, 1);
        }

        i = this.rand.nextInt(3) + this.rand.nextInt(1 + lootingModifier);

        for (int k = 0; k < i; ++k)
        {
            this.dropItem(Items.gunpowder, 1);
        }
    }

    protected float getSoundVolume()
    {
        return 10.0F;
    }

    public boolean getCanSpawnHere()
    {
        return this.rand.nextInt(20) == 0 && super.getCanSpawnHere() && this.worldObj.getDifficulty() != EnumDifficulty.PEACEFUL;
    }

    public int getMaxSpawnedInChunk()
    {
        return 1;
    }

    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("ExplosionPower", this.explosionStrength);
    }

    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("ExplosionPower", 99))
        {
            this.explosionStrength = tagCompund.getInteger("ExplosionPower");
        }
    }

    public float getEyeHeight()
    {
        return 2.6F;
    }

    static class AIFireballAttack extends EntityAIBase
    {
        private EntityGhast parentEntity;
        public int attackTimer;

        public AIFireballAttack(EntityGhast ghast)
        {
            this.parentEntity = ghast;
        }

        public boolean shouldExecute()
        {
            return this.parentEntity.getAttackTarget() != null;
        }

        public void startExecuting()
        {
            this.attackTimer = 0;
        }

        public void resetTask()
        {
            this.parentEntity.setAttacking(false);
        }

        public void updateTask()
        {
            EntityLivingBase entityLivingBase = this.parentEntity.getAttackTarget();
            double doubleValue = 64.0D;

            if (entityLivingBase.getDistanceSqToEntity(this.parentEntity) < doubleValue * doubleValue && this.parentEntity.canEntityBeSeen(entityLivingBase))
            {
                World world = this.parentEntity.worldObj;
                ++this.attackTimer;

                if (this.attackTimer == 10)
                {
                    world.playAuxSFXAtEntity((EntityPlayer)null, 1007, new BlockPos(this.parentEntity), 0);
                }

                if (this.attackTimer == 20)
                {
                    double doubleValue2 = 4.0D;
                    Vec3 localValue = this.parentEntity.getLook(1.0F);
                    double secondDoubleValue = entityLivingBase.posX - (this.parentEntity.posX + localValue.xCoord * doubleValue2);
                    double thirdDoubleValue = entityLivingBase.getEntityBoundingBox().minY + (double)(entityLivingBase.height / 2.0F) - (0.5D + this.parentEntity.posY + (double)(this.parentEntity.height / 2.0F));
                    double fourthDoubleValue = entityLivingBase.posZ - (this.parentEntity.posZ + localValue.zCoord * doubleValue2);
                    world.playAuxSFXAtEntity((EntityPlayer)null, 1008, new BlockPos(this.parentEntity), 0);
                    EntityLargeFireball entityLargeFireball = new EntityLargeFireball(world, this.parentEntity, secondDoubleValue, thirdDoubleValue, fourthDoubleValue);
                    entityLargeFireball.explosionPower = this.parentEntity.getFireballStrength();
                    entityLargeFireball.posX = this.parentEntity.posX + localValue.xCoord * doubleValue2;
                    entityLargeFireball.posY = this.parentEntity.posY + (double)(this.parentEntity.height / 2.0F) + 0.5D;
                    entityLargeFireball.posZ = this.parentEntity.posZ + localValue.zCoord * doubleValue2;
                    world.spawnEntityInWorld(entityLargeFireball);
                    this.attackTimer = -40;
                }
            }
            else if (this.attackTimer > 0)
            {
                --this.attackTimer;
            }

            this.parentEntity.setAttacking(this.attackTimer > 10);
        }
    }

    static class AILookAround extends EntityAIBase
    {
        private EntityGhast parentEntity;

        public AILookAround(EntityGhast ghast)
        {
            this.parentEntity = ghast;
            this.setMutexBits(2);
        }

        public boolean shouldExecute()
        {
            return true;
        }

        public void updateTask()
        {
            if (this.parentEntity.getAttackTarget() == null)
            {
                this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw = -((float)MathHelper.atan2(this.parentEntity.motionX, this.parentEntity.motionZ)) * 180.0F / (float)Math.PI;
            }
            else
            {
                EntityLivingBase entityLivingBase = this.parentEntity.getAttackTarget();
                double doubleValue = 64.0D;

                if (entityLivingBase.getDistanceSqToEntity(this.parentEntity) < doubleValue * doubleValue)
                {
                    double xCoordinate = entityLivingBase.posX - this.parentEntity.posX;
                    double zCoordinate = entityLivingBase.posZ - this.parentEntity.posZ;
                    this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw = -((float)MathHelper.atan2(xCoordinate, zCoordinate)) * 180.0F / (float)Math.PI;
                }
            }
        }
    }

    static class AIRandomFly extends EntityAIBase
    {
        private EntityGhast parentEntity;

        public AIRandomFly(EntityGhast ghast)
        {
            this.parentEntity = ghast;
            this.setMutexBits(1);
        }

        public boolean shouldExecute()
        {
            EntityMoveHelper entityMoveHelper = this.parentEntity.getMoveHelper();

            if (!entityMoveHelper.isUpdating())
            {
                return true;
            }
            else
            {
                double xCoordinate = entityMoveHelper.getX() - this.parentEntity.posX;
                double yCoordinate = entityMoveHelper.getY() - this.parentEntity.posY;
                double zCoordinate = entityMoveHelper.getZ() - this.parentEntity.posZ;
                double doubleValue = xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate;
                return doubleValue < 1.0D || doubleValue > 3600.0D;
            }
        }

        public boolean continueExecuting()
        {
            return false;
        }

        public void startExecuting()
        {
            Random random = this.parentEntity.getRNG();
            double xCoordinate = this.parentEntity.posX + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double yCoordinate = this.parentEntity.posY + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double zCoordinate = this.parentEntity.posZ + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            this.parentEntity.getMoveHelper().setMoveTo(xCoordinate, yCoordinate, zCoordinate, 1.0D);
        }
    }

    static class GhastMoveHelper extends EntityMoveHelper
    {
        private EntityGhast parentEntity;
        private int courseChangeCooldown;

        public GhastMoveHelper(EntityGhast ghast)
        {
            super(ghast);
            this.parentEntity = ghast;
        }

        public void onUpdateMoveHelper()
        {
            if (this.update)
            {
                double xCoordinate = this.posX - this.parentEntity.posX;
                double yCoordinate = this.posY - this.parentEntity.posY;
                double zCoordinate = this.posZ - this.parentEntity.posZ;
                double doubleValue = xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate;

                if (this.courseChangeCooldown-- <= 0)
                {
                    this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
                    doubleValue = (double)MathHelper.sqrt_double(doubleValue);

                    if (this.isNotColliding(this.posX, this.posY, this.posZ, doubleValue))
                    {
                        this.parentEntity.motionX += xCoordinate / doubleValue * 0.1D;
                        this.parentEntity.motionY += yCoordinate / doubleValue * 0.1D;
                        this.parentEntity.motionZ += zCoordinate / doubleValue * 0.1D;
                    }
                    else
                    {
                        this.update = false;
                    }
                }
            }
        }

        private boolean isNotColliding(double x, double y, double z, double distance)
        {
            double xCoordinate = (x - this.parentEntity.posX) / distance;
            double yCoordinate = (y - this.parentEntity.posY) / distance;
            double zCoordinate = (z - this.parentEntity.posZ) / distance;
            AxisAlignedBB axisAlignedBB = this.parentEntity.getEntityBoundingBox();

            for (int i = 1; (double)i < distance; ++i)
            {
                axisAlignedBB = axisAlignedBB.offset(xCoordinate, yCoordinate, zCoordinate);

                if (!this.parentEntity.worldObj.getCollidingBoundingBoxes(this.parentEntity, axisAlignedBB).isEmpty())
                {
                    return false;
                }
            }

            return true;
        }
    }
}
