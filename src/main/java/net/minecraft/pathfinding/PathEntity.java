package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class PathEntity
{
    private final PathPoint[] points;
    private int currentPathIndex;
    private int pathLength;

    public PathEntity(PathPoint[] pathPoints)
    {
        this.points = pathPoints;
        this.pathLength = pathPoints.length;
    }

    public void incrementPathIndex()
    {
        ++this.currentPathIndex;
    }

    public boolean isFinished()
    {
        return this.currentPathIndex >= this.pathLength;
    }

    public PathPoint getFinalPathPoint()
    {
        return this.pathLength > 0 ? this.points[this.pathLength - 1] : null;
    }

    public PathPoint getPathPointFromIndex(int index)
    {
        return this.points[index];
    }

    public int getCurrentPathLength()
    {
        return this.pathLength;
    }

    public void setCurrentPathLength(int length)
    {
        this.pathLength = length;
    }

    public int getCurrentPathIndex()
    {
        return this.currentPathIndex;
    }

    public void setCurrentPathIndex(int currentPathIndexIn)
    {
        this.currentPathIndex = currentPathIndexIn;
    }

    public Vec3 getVectorFromIndex(Entity entityIn, int index)
    {
        double x = (double)this.points[index].xCoord + (double)((int)(entityIn.width + 1.0F)) * 0.5D;
        double y = (double)this.points[index].yCoord;
        double z = (double)this.points[index].zCoord + (double)((int)(entityIn.width + 1.0F)) * 0.5D;
        return new Vec3(x, y, z);
    }

    public Vec3 getPosition(Entity entityIn)
    {
        return this.getVectorFromIndex(entityIn, this.currentPathIndex);
    }

    public boolean isSamePath(PathEntity otherPath)
    {
        if (otherPath == null)
        {
            return false;
        }
        else if (otherPath.points.length != this.points.length)
        {
            return false;
        }
        else
        {
            for (int pointIndex = 0; pointIndex < this.points.length; ++pointIndex)
            {
                if (this.points[pointIndex].xCoord != otherPath.points[pointIndex].xCoord || this.points[pointIndex].yCoord != otherPath.points[pointIndex].yCoord || this.points[pointIndex].zCoord != otherPath.points[pointIndex].zCoord)
                {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean isDestinationSame(Vec3 position)
    {
        PathPoint finalPoint = this.getFinalPathPoint();
        return finalPoint == null ? false : finalPoint.xCoord == (int)position.xCoord && finalPoint.zCoord == (int)position.zCoord;
    }
}
