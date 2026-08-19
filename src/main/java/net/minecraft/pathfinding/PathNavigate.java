package net.minecraft.pathfinding;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

public abstract class PathNavigate
{
    protected EntityLiving theEntity;
    protected World worldObj;
    protected PathEntity currentPath;
    protected double speed;
    private final IAttributeInstance pathSearchRange;
    private int totalTicks;
    private int ticksAtLastPos;
    private Vec3 lastPosCheck = new Vec3(0.0D, 0.0D, 0.0D);
    private float heightRequirement = 1.0F;
    private final PathFinder pathFinder;

    public PathNavigate(EntityLiving entitylivingIn, World worldIn)
    {
        this.theEntity = entitylivingIn;
        this.worldObj = worldIn;
        this.pathSearchRange = entitylivingIn.getEntityAttribute(SharedMonsterAttributes.followRange);
        this.pathFinder = this.getPathFinder();
    }

    protected abstract PathFinder getPathFinder();

    public void setSpeed(double speedIn)
    {
        this.speed = speedIn;
    }

    public float getPathSearchRange()
    {
        return (float)this.pathSearchRange.getAttributeValue();
    }

    public final PathEntity getPathToXYZ(double x, double y, double z)
    {
        return this.getPathToPos(new BlockPos(MathHelper.floor_double(x), (int)y, MathHelper.floor_double(z)));
    }

    public PathEntity getPathToPos(BlockPos pos)
    {
        if (!this.canNavigate())
        {
            return null;
        }
        else
        {
            float searchRange = this.getPathSearchRange();
            this.worldObj.theProfiler.startSection("pathfind");
            BlockPos entityPos = new BlockPos(this.theEntity);
            int cacheRadius = (int)(searchRange + 8.0F);
            ChunkCache chunkCache = new ChunkCache(this.worldObj, entityPos.add(-cacheRadius, -cacheRadius, -cacheRadius), entityPos.add(cacheRadius, cacheRadius, cacheRadius), 0);
            PathEntity path = this.pathFinder.createEntityPathTo(chunkCache, this.theEntity, pos, searchRange);
            this.worldObj.theProfiler.endSection();
            return path;
        }
    }

    public boolean tryMoveToXYZ(double x, double y, double z, double speedIn)
    {
        PathEntity path = this.getPathToXYZ((double)MathHelper.floor_double(x), (double)((int)y), (double)MathHelper.floor_double(z));
        return this.setPath(path, speedIn);
    }

    public void setHeightRequirement(float jumpHeight)
    {
        this.heightRequirement = jumpHeight;
    }

    public PathEntity getPathToEntityLiving(Entity entityIn)
    {
        if (!this.canNavigate())
        {
            return null;
        }
        else
        {
            float searchRange = this.getPathSearchRange();
            this.worldObj.theProfiler.startSection("pathfind");
            BlockPos entityPos = (new BlockPos(this.theEntity)).up();
            int cacheRadius = (int)(searchRange + 16.0F);
            ChunkCache chunkCache = new ChunkCache(this.worldObj, entityPos.add(-cacheRadius, -cacheRadius, -cacheRadius), entityPos.add(cacheRadius, cacheRadius, cacheRadius), 0);
            PathEntity path = this.pathFinder.createEntityPathTo(chunkCache, this.theEntity, entityIn, searchRange);
            this.worldObj.theProfiler.endSection();
            return path;
        }
    }

    public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn)
    {
        PathEntity path = this.getPathToEntityLiving(entityIn);
        return path != null ? this.setPath(path, speedIn) : false;
    }

    public boolean setPath(PathEntity path, double speedIn)
    {
        if (path == null)
        {
            this.currentPath = null;
            return false;
        }
        else
        {
            if (!path.isSamePath(this.currentPath))
            {
                this.currentPath = path;
            }

            this.removeSunnyPath();

            if (this.currentPath.getCurrentPathLength() == 0)
            {
                return false;
            }
            else
            {
                this.speed = speedIn;
                Vec3 currentPosition = this.getEntityPosition();
                this.ticksAtLastPos = this.totalTicks;
                this.lastPosCheck = currentPosition;
                return true;
            }
        }
    }

    public PathEntity getPath()
    {
        return this.currentPath;
    }

    public void onUpdateNavigation()
    {
        ++this.totalTicks;

        if (!this.noPath())
        {
            if (this.canNavigate())
            {
                this.pathFollow();
            }
            else if (this.currentPath != null && this.currentPath.getCurrentPathIndex() < this.currentPath.getCurrentPathLength())
            {
                Vec3 currentPosition = this.getEntityPosition();
                Vec3 nextPathPosition = this.currentPath.getVectorFromIndex(this.theEntity, this.currentPath.getCurrentPathIndex());

                if (currentPosition.yCoord > nextPathPosition.yCoord && !this.theEntity.onGround && MathHelper.floor_double(currentPosition.xCoord) == MathHelper.floor_double(nextPathPosition.xCoord) && MathHelper.floor_double(currentPosition.zCoord) == MathHelper.floor_double(nextPathPosition.zCoord))
                {
                    this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
                }
            }

            if (!this.noPath())
            {
                Vec3 pathPosition = this.currentPath.getPosition(this.theEntity);

                if (pathPosition != null)
                {
                    AxisAlignedBB pathPointBounds = (new AxisAlignedBB(pathPosition.xCoord, pathPosition.yCoord, pathPosition.zCoord, pathPosition.xCoord, pathPosition.yCoord, pathPosition.zCoord)).expand(0.5D, 0.5D, 0.5D);
                    List<AxisAlignedBB> collisionBoxes = this.worldObj.getCollidingBoundingBoxes(this.theEntity, pathPointBounds.addCoord(0.0D, -1.0D, 0.0D));
                    double yOffset = -1.0D;
                    pathPointBounds = pathPointBounds.offset(0.0D, 1.0D, 0.0D);

                    for (AxisAlignedBB collisionBox : collisionBoxes)
                    {
                        yOffset = collisionBox.calculateYOffset(pathPointBounds, yOffset);
                    }

                    this.theEntity.getMoveHelper().setMoveTo(pathPosition.xCoord, pathPosition.yCoord + yOffset, pathPosition.zCoord, this.speed);
                }
            }
        }
    }

    protected void pathFollow()
    {
        Vec3 currentPosition = this.getEntityPosition();
        int sameYPathLength = this.currentPath.getCurrentPathLength();

        for (int pathIndex = this.currentPath.getCurrentPathIndex(); pathIndex < this.currentPath.getCurrentPathLength(); ++pathIndex)
        {
            if (this.currentPath.getPathPointFromIndex(pathIndex).yCoord != (int)currentPosition.yCoord)
            {
                sameYPathLength = pathIndex;
                break;
            }
        }

        float maxDistanceSquared = this.theEntity.width * this.theEntity.width * this.heightRequirement;

        for (int pathIndex = this.currentPath.getCurrentPathIndex(); pathIndex < sameYPathLength; ++pathIndex)
        {
            Vec3 pathPosition = this.currentPath.getVectorFromIndex(this.theEntity, pathIndex);

            if (currentPosition.squareDistanceTo(pathPosition) < (double)maxDistanceSquared)
            {
                this.currentPath.setCurrentPathIndex(pathIndex + 1);
            }
        }

        int entityWidth = MathHelper.ceiling_float_int(this.theEntity.width);
        int entityHeight = (int)this.theEntity.height + 1;
        int entityDepth = entityWidth;

        for (int pathIndex = sameYPathLength - 1; pathIndex >= this.currentPath.getCurrentPathIndex(); --pathIndex)
        {
            if (this.isDirectPathBetweenPoints(currentPosition, this.currentPath.getVectorFromIndex(this.theEntity, pathIndex), entityWidth, entityHeight, entityDepth))
            {
                this.currentPath.setCurrentPathIndex(pathIndex);
                break;
            }
        }

        this.checkForStuck(currentPosition);
    }

    protected void checkForStuck(Vec3 currentPosition)
    {
        if (this.totalTicks - this.ticksAtLastPos > 100)
        {
            if (currentPosition.squareDistanceTo(this.lastPosCheck) < 2.25D)
            {
                this.clearPathEntity();
            }

            this.ticksAtLastPos = this.totalTicks;
            this.lastPosCheck = currentPosition;
        }
    }

    public boolean noPath()
    {
        return this.currentPath == null || this.currentPath.isFinished();
    }

    public void clearPathEntity()
    {
        this.currentPath = null;
    }

    protected abstract Vec3 getEntityPosition();

    protected abstract boolean canNavigate();

    protected boolean isInLiquid()
    {
        return this.theEntity.isInWater() || this.theEntity.isInLava();
    }

    protected void removeSunnyPath()
    {
    }

    protected abstract boolean isDirectPathBetweenPoints(Vec3 startVec, Vec3 endVec, int sizeX, int sizeY, int sizeZ);
}
