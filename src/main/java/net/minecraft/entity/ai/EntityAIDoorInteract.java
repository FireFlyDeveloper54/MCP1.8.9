package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;

public abstract class EntityAIDoorInteract extends EntityAIBase
{
    protected EntityLiving theEntity;
    protected BlockPos doorPosition = BlockPos.ORIGIN;
    protected BlockDoor doorBlock;
    boolean hasStoppedDoorInteraction;
    float entityPositionX;
    float entityPositionZ;

    public EntityAIDoorInteract(EntityLiving entityIn)
    {
        this.theEntity = entityIn;

        if (!(entityIn.getNavigator() instanceof PathNavigateGround))
        {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
    }

    public boolean shouldExecute()
    {
        if (!this.theEntity.isCollidedHorizontally)
        {
            return false;
        }
        else
        {
            PathNavigateGround pathNavigateGround = (PathNavigateGround)this.theEntity.getNavigator();
            PathEntity pathEntity = pathNavigateGround.getPath();

            if (pathEntity != null && !pathEntity.isFinished() && pathNavigateGround.getEnterDoors())
            {
                for (int pathIndex = 0; pathIndex < Math.min(pathEntity.getCurrentPathIndex() + 2, pathEntity.getCurrentPathLength()); ++pathIndex)
                {
                    PathPoint pathPoint = pathEntity.getPathPointFromIndex(pathIndex);
                    this.doorPosition = new BlockPos(pathPoint.xCoord, pathPoint.yCoord + 1, pathPoint.zCoord);

                    if (this.theEntity.getDistanceSq((double)this.doorPosition.getX(), this.theEntity.posY, (double)this.doorPosition.getZ()) <= 2.25D)
                    {
                        this.doorBlock = this.getBlockDoor(this.doorPosition);

                        if (this.doorBlock != null)
                        {
                            return true;
                        }
                    }
                }

                this.doorPosition = new BlockPos(this.theEntity.posX, this.theEntity.posY + 1.0D, this.theEntity.posZ);
                this.doorBlock = this.getBlockDoor(this.doorPosition);
                return this.doorBlock != null;
            }
            else
            {
                return false;
            }
        }
    }

    public boolean continueExecuting()
    {
        return !this.hasStoppedDoorInteraction;
    }

    public void startExecuting()
    {
        this.hasStoppedDoorInteraction = false;
        this.entityPositionX = (float)((double)((float)this.doorPosition.getX() + 0.5F) - this.theEntity.posX);
        this.entityPositionZ = (float)((double)((float)this.doorPosition.getZ() + 0.5F) - this.theEntity.posZ);
    }

    public void updateTask()
    {
        float deltaX = (float)((double)((float)this.doorPosition.getX() + 0.5F) - this.theEntity.posX);
        float deltaZ = (float)((double)((float)this.doorPosition.getZ() + 0.5F) - this.theEntity.posZ);
        float dotProduct = this.entityPositionX * deltaX + this.entityPositionZ * deltaZ;

        if (dotProduct < 0.0F)
        {
            this.hasStoppedDoorInteraction = true;
        }
    }

    private BlockDoor getBlockDoor(BlockPos pos)
    {
        Block block = this.theEntity.worldObj.getBlockState(pos).getBlock();
        return block instanceof BlockDoor && block.getMaterial() == Material.wood ? (BlockDoor)block : null;
    }
}
