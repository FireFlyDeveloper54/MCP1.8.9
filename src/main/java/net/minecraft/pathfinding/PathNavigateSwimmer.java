package net.minecraft.pathfinding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.pathfinder.SwimNodeProcessor;

public class PathNavigateSwimmer extends PathNavigate
{
    public PathNavigateSwimmer(EntityLiving entitylivingIn, World worldIn)
    {
        super(entitylivingIn, worldIn);
    }

    protected PathFinder getPathFinder()
    {
        return new PathFinder(new SwimNodeProcessor());
    }

    protected boolean canNavigate()
    {
        return this.isInLiquid();
    }

    protected Vec3 getEntityPosition()
    {
        return new Vec3(this.theEntity.posX, this.theEntity.posY + (double)this.theEntity.height * 0.5D, this.theEntity.posZ);
    }

    protected void pathFollow()
    {
        Vec3 currentPosition = this.getEntityPosition();
        float maxDistanceSquared = this.theEntity.width * this.theEntity.width;
        int lookAheadDistance = 6;

        if (currentPosition.squareDistanceTo(this.currentPath.getVectorFromIndex(this.theEntity, this.currentPath.getCurrentPathIndex())) < (double)maxDistanceSquared)
        {
            this.currentPath.incrementPathIndex();
        }

        for (int pathIndex = Math.min(this.currentPath.getCurrentPathIndex() + lookAheadDistance, this.currentPath.getCurrentPathLength() - 1); pathIndex > this.currentPath.getCurrentPathIndex(); --pathIndex)
        {
            Vec3 pathPosition = this.currentPath.getVectorFromIndex(this.theEntity, pathIndex);

            if (pathPosition.squareDistanceTo(currentPosition) <= 36.0D && this.isDirectPathBetweenPoints(currentPosition, pathPosition, 0, 0, 0))
            {
                this.currentPath.setCurrentPathIndex(pathIndex);
                break;
            }
        }

        this.checkForStuck(currentPosition);
    }

    protected void removeSunnyPath()
    {
        super.removeSunnyPath();
    }

    protected boolean isDirectPathBetweenPoints(Vec3 startVec, Vec3 endVec, int sizeX, int sizeY, int sizeZ)
    {
        MovingObjectPosition hitResult = this.worldObj.rayTraceBlocks(startVec, new Vec3(endVec.xCoord, endVec.yCoord + (double)this.theEntity.height * 0.5D, endVec.zCoord), false, true, false);
        return hitResult == null || hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.MISS;
    }
}
