package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAIOwnerHurtTarget extends EntityAITarget
{
    EntityTameable theEntityTameable;
    EntityLivingBase theTarget;
    private int timestamp;

    public EntityAIOwnerHurtTarget(EntityTameable theEntityTameableIn)
    {
        super(theEntityTameableIn, false);
        this.theEntityTameable = theEntityTameableIn;
        this.setMutexBits(1);
    }

    public boolean shouldExecute()
    {
        if (!this.theEntityTameable.isTamed())
        {
            return false;
        }
        else
        {
            EntityLivingBase entityLivingBase = this.theEntityTameable.getOwner();

            if (entityLivingBase == null)
            {
                return false;
            }
            else
            {
                this.theTarget = entityLivingBase.getLastAttacker();
                int lastAttackerTime = entityLivingBase.getLastAttackerTime();
                return lastAttackerTime != this.timestamp && this.isSuitableTarget(this.theTarget, false) && this.theEntityTameable.shouldAttackEntity(this.theTarget, entityLivingBase);
            }
        }
    }

    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.theTarget);
        EntityLivingBase entityLivingBase = this.theEntityTameable.getOwner();

        if (entityLivingBase != null)
        {
            this.timestamp = entityLivingBase.getLastAttackerTime();
        }

        super.startExecuting();
    }
}
