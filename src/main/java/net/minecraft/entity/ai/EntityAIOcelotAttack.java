package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityAIOcelotAttack extends EntityAIBase
{
    World theWorld;
    EntityLiving theEntity;
    EntityLivingBase theVictim;
    int attackCountdown;

    public EntityAIOcelotAttack(EntityLiving theEntityIn)
    {
        this.theEntity = theEntityIn;
        this.theWorld = theEntityIn.worldObj;
        this.setMutexBits(3);
    }

    public boolean shouldExecute()
    {
        EntityLivingBase entityLivingBase = this.theEntity.getAttackTarget();

        if (entityLivingBase == null)
        {
            return false;
        }
        else
        {
            this.theVictim = entityLivingBase;
            return true;
        }
    }

    public boolean continueExecuting()
    {
        return !this.theVictim.isEntityAlive() ? false : (this.theEntity.getDistanceSqToEntity(this.theVictim) > 225.0D ? false : !this.theEntity.getNavigator().noPath() || this.shouldExecute());
    }

    public void resetTask()
    {
        this.theVictim = null;
        this.theEntity.getNavigator().clearPathEntity();
    }

    public void updateTask()
    {
        this.theEntity.getLookHelper().setLookPositionWithEntity(this.theVictim, 30.0F, 30.0F);
        double attackReachSq = (double)(this.theEntity.width * 2.0F * this.theEntity.width * 2.0F);
        double victimDistanceSq = this.theEntity.getDistanceSq(this.theVictim.posX, this.theVictim.getEntityBoundingBox().minY, this.theVictim.posZ);
        double moveSpeed = 0.8D;

        if (victimDistanceSq > attackReachSq && victimDistanceSq < 16.0D)
        {
            moveSpeed = 1.33D;
        }
        else if (victimDistanceSq < 225.0D)
        {
            moveSpeed = 0.6D;
        }

        this.theEntity.getNavigator().tryMoveToEntityLiving(this.theVictim, moveSpeed);
        this.attackCountdown = Math.max(this.attackCountdown - 1, 0);

        if (victimDistanceSq <= attackReachSq)
        {
            if (this.attackCountdown <= 0)
            {
                this.attackCountdown = 20;
                this.theEntity.attackEntityAsMob(this.theVictim);
            }
        }
    }
}
