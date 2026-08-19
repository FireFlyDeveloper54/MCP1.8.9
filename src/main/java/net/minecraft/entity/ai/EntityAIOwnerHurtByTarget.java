package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAIOwnerHurtByTarget extends EntityAITarget
{
    EntityTameable theDefendingTameable;
    EntityLivingBase theOwnerAttacker;
    private int timestamp;

    public EntityAIOwnerHurtByTarget(EntityTameable theDefendingTameableIn)
    {
        super(theDefendingTameableIn, false);
        this.theDefendingTameable = theDefendingTameableIn;
        this.setMutexBits(1);
    }

    public boolean shouldExecute()
    {
        if (!this.theDefendingTameable.isTamed())
        {
            return false;
        }
        else
        {
            EntityLivingBase entityLivingBase = this.theDefendingTameable.getOwner();

            if (entityLivingBase == null)
            {
                return false;
            }
            else
            {
                this.theOwnerAttacker = entityLivingBase.getAITarget();
                int revengeTimer = entityLivingBase.getRevengeTimer();
                return revengeTimer != this.timestamp && this.isSuitableTarget(this.theOwnerAttacker, false) && this.theDefendingTameable.shouldAttackEntity(this.theOwnerAttacker, entityLivingBase);
            }
        }
    }

    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.theOwnerAttacker);
        EntityLivingBase entityLivingBase = this.theDefendingTameable.getOwner();

        if (entityLivingBase != null)
        {
            this.timestamp = entityLivingBase.getRevengeTimer();
        }

        super.startExecuting();
    }
}
