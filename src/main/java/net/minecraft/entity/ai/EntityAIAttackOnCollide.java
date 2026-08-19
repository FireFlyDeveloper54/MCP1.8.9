package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityAIAttackOnCollide extends EntityAIBase
{
    World worldObj;
    protected EntityCreature attacker;
    int attackTick;
    double speedTowardsTarget;
    boolean longMemory;
    PathEntity entityPathEntity;
    Class <? extends Entity > classTarget;
    private int delayCounter;
    private double targetX;
    private double targetY;
    private double targetZ;

    public EntityAIAttackOnCollide(EntityCreature creature, Class <? extends Entity > targetClass, double speedIn, boolean useLongMemory)
    {
        this(creature, speedIn, useLongMemory);
        this.classTarget = targetClass;
    }

    public EntityAIAttackOnCollide(EntityCreature creature, double speedIn, boolean useLongMemory)
    {
        this.attacker = creature;
        this.worldObj = creature.worldObj;
        this.speedTowardsTarget = speedIn;
        this.longMemory = useLongMemory;
        this.setMutexBits(3);
    }

    public boolean shouldExecute()
    {
        EntityLivingBase entityLivingBase = this.attacker.getAttackTarget();

        if (entityLivingBase == null)
        {
            return false;
        }
        else if (!entityLivingBase.isEntityAlive())
        {
            return false;
        }
        else if (this.classTarget != null && !this.classTarget.isAssignableFrom(entityLivingBase.getClass()))
        {
            return false;
        }
        else
        {
            this.entityPathEntity = this.attacker.getNavigator().getPathToEntityLiving(entityLivingBase);
            return this.entityPathEntity != null;
        }
    }

    public boolean continueExecuting()
    {
        EntityLivingBase entityLivingBase = this.attacker.getAttackTarget();
        return entityLivingBase == null ? false : (!entityLivingBase.isEntityAlive() ? false : (!this.longMemory ? !this.attacker.getNavigator().noPath() : this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(entityLivingBase))));
    }

    public void startExecuting()
    {
        this.attacker.getNavigator().setPath(this.entityPathEntity, this.speedTowardsTarget);
        this.delayCounter = 0;
    }

    public void resetTask()
    {
        this.attacker.getNavigator().clearPathEntity();
    }

    public void updateTask()
    {
        EntityLivingBase entityLivingBase = this.attacker.getAttackTarget();
        this.attacker.getLookHelper().setLookPositionWithEntity(entityLivingBase, 30.0F, 30.0F);
        double distanceSq = this.attacker.getDistanceSq(entityLivingBase.posX, entityLivingBase.getEntityBoundingBox().minY, entityLivingBase.posZ);
        double attackReachSq = this.getAttackReachSqr(entityLivingBase);
        --this.delayCounter;

        if ((this.longMemory || this.attacker.getEntitySenses().canSee(entityLivingBase)) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || entityLivingBase.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= 1.0D || this.attacker.getRNG().nextFloat() < 0.05F))
        {
            this.targetX = entityLivingBase.posX;
            this.targetY = entityLivingBase.getEntityBoundingBox().minY;
            this.targetZ = entityLivingBase.posZ;
            this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);

            if (distanceSq > 1024.0D)
            {
                this.delayCounter += 10;
            }
            else if (distanceSq > 256.0D)
            {
                this.delayCounter += 5;
            }

            if (!this.attacker.getNavigator().tryMoveToEntityLiving(entityLivingBase, this.speedTowardsTarget))
            {
                this.delayCounter += 15;
            }
        }

        this.attackTick = Math.max(this.attackTick - 1, 0);

        if (distanceSq <= attackReachSq && this.attackTick <= 0)
        {
            this.attackTick = 20;

            if (this.attacker.getHeldItem() != null)
            {
                this.attacker.swingItem();
            }

            this.attacker.attackEntityAsMob(entityLivingBase);
        }
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget)
    {
        return (double)(this.attacker.width * 2.0F * this.attacker.width * 2.0F + attackTarget.width);
    }
}
