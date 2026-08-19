package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

public class EntityAIMoveIndoors extends EntityAIBase
{
    private EntityCreature entityObj;
    private VillageDoorInfo doorInfo;
    private int insidePosX = -1;
    private int insidePosZ = -1;

    public EntityAIMoveIndoors(EntityCreature entityObjIn)
    {
        this.entityObj = entityObjIn;
        this.setMutexBits(1);
    }

    public boolean shouldExecute()
    {
        BlockPos blockPos = new BlockPos(this.entityObj);

        if ((!this.entityObj.worldObj.isDaytime() || this.entityObj.worldObj.isRaining() && !this.entityObj.worldObj.getBiomeGenForCoords(blockPos).canRain()) && !this.entityObj.worldObj.provider.getHasNoSky())
        {
            if (this.entityObj.getRNG().nextInt(50) != 0)
            {
                return false;
            }
            else if (this.insidePosX != -1 && this.entityObj.getDistanceSq((double)this.insidePosX, this.entityObj.posY, (double)this.insidePosZ) < 4.0D)
            {
                return false;
            }
            else
            {
                Village village = this.entityObj.worldObj.getVillageCollection().getNearestVillage(blockPos, 14);

                if (village == null)
                {
                    return false;
                }
                else
                {
                    this.doorInfo = village.getDoorInfo(blockPos);
                    return this.doorInfo != null;
                }
            }
        }
        else
        {
            return false;
        }
    }

    public boolean continueExecuting()
    {
        return !this.entityObj.getNavigator().noPath();
    }

    public void startExecuting()
    {
        this.insidePosX = -1;
        BlockPos blockPos = this.doorInfo.getInsideBlockPos();
        int insideX = blockPos.getX();
        int insideY = blockPos.getY();
        int insideZ = blockPos.getZ();

        if (this.entityObj.getDistanceSq(blockPos) > 256.0D)
        {
            Vec3 targetPos = RandomPositionGenerator.findRandomTargetBlockTowards(this.entityObj, 14, 3, new Vec3((double)insideX + 0.5D, (double)insideY, (double)insideZ + 0.5D));

            if (targetPos != null)
            {
                this.entityObj.getNavigator().tryMoveToXYZ(targetPos.xCoord, targetPos.yCoord, targetPos.zCoord, 1.0D);
            }
        }
        else
        {
            this.entityObj.getNavigator().tryMoveToXYZ((double)insideX + 0.5D, (double)insideY, (double)insideZ + 0.5D, 1.0D);
        }
    }

    public void resetTask()
    {
        this.insidePosX = this.doorInfo.getInsideBlockPos().getX();
        this.insidePosZ = this.doorInfo.getInsideBlockPos().getZ();
        this.doorInfo = null;
    }
}
