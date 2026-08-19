package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.Vec3;

public class EntityAIPanic extends EntityAIBase
{
    private EntityCreature theEntityCreature;
    protected double speed;
    private double randPosX;
    private double randPosY;
    private double randPosZ;

    public EntityAIPanic(EntityCreature creature, double speedIn)
    {
        this.theEntityCreature = creature;
        this.speed = speedIn;
        this.setMutexBits(1);
    }

    public boolean shouldExecute()
    {
        if (this.theEntityCreature.getAITarget() == null && !this.theEntityCreature.isBurning())
        {
            return false;
        }
        else
        {
            Vec3 panicPos = RandomPositionGenerator.findRandomTarget(this.theEntityCreature, 5, 4);

            if (panicPos == null)
            {
                return false;
            }
            else
            {
                this.randPosX = panicPos.xCoord;
                this.randPosY = panicPos.yCoord;
                this.randPosZ = panicPos.zCoord;
                return true;
            }
        }
    }

    public void startExecuting()
    {
        this.theEntityCreature.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);
    }

    public boolean continueExecuting()
    {
        return !this.theEntityCreature.getNavigator().noPath();
    }
}
