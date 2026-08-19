package net.minecraft.entity.ai;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.Vec3;

public class EntityAIPlay extends EntityAIBase
{
    private EntityVillager villagerObj;
    private EntityLivingBase targetVillager;
    private double speed;
    private int playTime;

    public EntityAIPlay(EntityVillager villagerObjIn, double speedIn)
    {
        this.villagerObj = villagerObjIn;
        this.speed = speedIn;
        this.setMutexBits(1);
    }

    public boolean shouldExecute()
    {
        if (this.villagerObj.getGrowingAge() >= 0)
        {
            return false;
        }
        else if (this.villagerObj.getRNG().nextInt(400) != 0)
        {
            return false;
        }
        else
        {
            List<EntityVillager> list = this.villagerObj.worldObj.<EntityVillager>getEntitiesWithinAABB(EntityVillager.class, this.villagerObj.getEntityBoundingBox().expand(6.0D, 3.0D, 6.0D));
            double closestDistanceSq = Double.MAX_VALUE;

            for (EntityVillager entityVillager : list)
            {
                if (entityVillager != this.villagerObj && !entityVillager.isPlaying() && entityVillager.getGrowingAge() < 0)
                {
                    double candidateDistanceSq = entityVillager.getDistanceSqToEntity(this.villagerObj);

                    if (candidateDistanceSq <= closestDistanceSq)
                    {
                        closestDistanceSq = candidateDistanceSq;
                        this.targetVillager = entityVillager;
                    }
                }
            }

            if (this.targetVillager == null)
            {
                Vec3 playPos = RandomPositionGenerator.findRandomTarget(this.villagerObj, 16, 3);

                if (playPos == null)
                {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean continueExecuting()
    {
        return this.playTime > 0;
    }

    public void startExecuting()
    {
        if (this.targetVillager != null)
        {
            this.villagerObj.setPlaying(true);
        }

        this.playTime = 1000;
    }

    public void resetTask()
    {
        this.villagerObj.setPlaying(false);
        this.targetVillager = null;
    }

    public void updateTask()
    {
        --this.playTime;

        if (this.targetVillager != null)
        {
            if (this.villagerObj.getDistanceSqToEntity(this.targetVillager) > 4.0D)
            {
                this.villagerObj.getNavigator().tryMoveToEntityLiving(this.targetVillager, this.speed);
            }
        }
        else if (this.villagerObj.getNavigator().noPath())
        {
            Vec3 playPos = RandomPositionGenerator.findRandomTarget(this.villagerObj, 16, 3);

            if (playPos == null)
            {
                return;
            }

            this.villagerObj.getNavigator().tryMoveToXYZ(playPos.xCoord, playPos.yCoord, playPos.zCoord, this.speed);
        }
    }
}
