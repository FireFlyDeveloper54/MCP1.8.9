package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;

public class EntityAIHurtByTarget extends EntityAITarget
{
    private boolean entityCallsForHelp;
    private int revengeTimerOld;
    private final Class[] targetClasses;

    public EntityAIHurtByTarget(EntityCreature creatureIn, boolean entityCallsForHelpIn, Class... targetClassesIn)
    {
        super(creatureIn, false);
        this.entityCallsForHelp = entityCallsForHelpIn;
        this.targetClasses = targetClassesIn;
        this.setMutexBits(1);
    }

    public boolean shouldExecute()
    {
        int revengeTimer = this.taskOwner.getRevengeTimer();
        return revengeTimer != this.revengeTimerOld && this.isSuitableTarget(this.taskOwner.getAITarget(), false);
    }

    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.taskOwner.getAITarget());
        this.revengeTimerOld = this.taskOwner.getRevengeTimer();

        if (this.entityCallsForHelp)
        {
            double alertRange = this.getTargetDistance();

            for (EntityCreature nearbyCreature : this.taskOwner.worldObj.getEntitiesWithinAABB(this.taskOwner.getClass(), (new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ, this.taskOwner.posX + 1.0D, this.taskOwner.posY + 1.0D, this.taskOwner.posZ + 1.0D)).expand(alertRange, 10.0D, alertRange)))
            {
                if (this.taskOwner != nearbyCreature && nearbyCreature.getAttackTarget() == null && !nearbyCreature.isOnSameTeam(this.taskOwner.getAITarget()))
                {
                    boolean isExcludedClass = false;

                    for (Class targetClass : this.targetClasses)
                    {
                        if (nearbyCreature.getClass() == targetClass)
                        {
                            isExcludedClass = true;
                            break;
                        }
                    }

                    if (!isExcludedClass)
                    {
                        this.setEntityAttackTarget(nearbyCreature, this.taskOwner.getAITarget());
                    }
                }
            }
        }

        super.startExecuting();
    }

    protected void setEntityAttackTarget(EntityCreature creatureIn, EntityLivingBase entityLivingBaseIn)
    {
        creatureIn.setAttackTarget(entityLivingBaseIn);
    }
}
