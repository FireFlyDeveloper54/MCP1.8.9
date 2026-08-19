package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.pathfinder.NodeProcessor;

public class PathFinder
{
    private Path path = new Path();
    private PathPoint[] pathOptions = new PathPoint[32];
    private NodeProcessor nodeProcessor;

    public PathFinder(NodeProcessor nodeProcessorIn)
    {
        this.nodeProcessor = nodeProcessorIn;
    }

    public PathEntity createEntityPathTo(IBlockAccess blockAccess, Entity entityFrom, Entity entityTo, float maxDistance)
    {
        return this.createEntityPathTo(blockAccess, entityFrom, entityTo.posX, entityTo.getEntityBoundingBox().minY, entityTo.posZ, maxDistance);
    }

    public PathEntity createEntityPathTo(IBlockAccess blockAccess, Entity entityIn, BlockPos targetPos, float maxDistance)
    {
        return this.createEntityPathTo(blockAccess, entityIn, (double)((float)targetPos.getX() + 0.5F), (double)((float)targetPos.getY() + 0.5F), (double)((float)targetPos.getZ() + 0.5F), maxDistance);
    }

    private PathEntity createEntityPathTo(IBlockAccess blockAccess, Entity entityIn, double x, double y, double z, float maxDistance)
    {
        this.path.clearPath();
        this.nodeProcessor.initProcessor(blockAccess, entityIn);
        PathPoint startPoint = this.nodeProcessor.getPathPointTo(entityIn);
        PathPoint targetPoint = this.nodeProcessor.getPathPointToCoords(entityIn, x, y, z);
        PathEntity pathEntity = this.addToPath(entityIn, startPoint, targetPoint, maxDistance);
        this.nodeProcessor.postProcess();
        return pathEntity;
    }

    private PathEntity addToPath(Entity entityIn, PathPoint startPoint, PathPoint targetPoint, float maxDistance)
    {
        startPoint.totalPathDistance = 0.0F;
        startPoint.distanceToNext = startPoint.distanceToSquared(targetPoint);
        startPoint.distanceToTarget = startPoint.distanceToNext;
        this.path.clearPath();
        this.path.addPoint(startPoint);
        PathPoint closestPoint = startPoint;

        while (!this.path.isPathEmpty())
        {
            PathPoint currentPoint = this.path.dequeue();

            if (currentPoint.equals(targetPoint))
            {
                return this.createEntityPath(startPoint, targetPoint);
            }

            if (currentPoint.distanceToSquared(targetPoint) < closestPoint.distanceToSquared(targetPoint))
            {
                closestPoint = currentPoint;
            }

            currentPoint.visited = true;
            int optionCount = this.nodeProcessor.findPathOptions(this.pathOptions, entityIn, currentPoint, targetPoint, maxDistance);

            for (int optionIndex = 0; optionIndex < optionCount; ++optionIndex)
            {
                PathPoint optionPoint = this.pathOptions[optionIndex];
                float totalDistance = currentPoint.totalPathDistance + currentPoint.distanceToSquared(optionPoint);

                if (totalDistance < maxDistance * 2.0F && (!optionPoint.isAssigned() || totalDistance < optionPoint.totalPathDistance))
                {
                    optionPoint.previous = currentPoint;
                    optionPoint.totalPathDistance = totalDistance;
                    optionPoint.distanceToNext = optionPoint.distanceToSquared(targetPoint);

                    if (optionPoint.isAssigned())
                    {
                        this.path.changeDistance(optionPoint, optionPoint.totalPathDistance + optionPoint.distanceToNext);
                    }
                    else
                    {
                        optionPoint.distanceToTarget = optionPoint.totalPathDistance + optionPoint.distanceToNext;
                        this.path.addPoint(optionPoint);
                    }
                }
            }
        }

        if (closestPoint == startPoint)
        {
            return null;
        }
        else
        {
            return this.createEntityPath(startPoint, closestPoint);
        }
    }

    private PathEntity createEntityPath(PathPoint start, PathPoint end)
    {
        int pathLength = 1;

        for (PathPoint point = end; point.previous != null; point = point.previous)
        {
            ++pathLength;
        }

        PathPoint[] pathPoints = new PathPoint[pathLength];
        PathPoint point = end;
        --pathLength;

        for (pathPoints[pathLength] = end; point.previous != null; pathPoints[pathLength] = point)
        {
            point = point.previous;
            --pathLength;
        }

        return new PathEntity(pathPoints);
    }
}
