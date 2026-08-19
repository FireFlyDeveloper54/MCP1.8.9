package net.minecraft.pathfinding;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.pathfinder.WalkNodeProcessor;

public class PathNavigateGround extends PathNavigate
{
    protected WalkNodeProcessor nodeProcessor;
    private boolean shouldAvoidSun;

    public PathNavigateGround(EntityLiving entitylivingIn, World worldIn)
    {
        super(entitylivingIn, worldIn);
    }

    protected PathFinder getPathFinder()
    {
        this.nodeProcessor = new WalkNodeProcessor();
        this.nodeProcessor.setEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }

    protected boolean canNavigate()
    {
        return this.theEntity.onGround || this.getCanSwim() && this.isInLiquid() || this.theEntity.isRiding() && this.theEntity instanceof EntityZombie && this.theEntity.ridingEntity instanceof EntityChicken;
    }

    protected Vec3 getEntityPosition()
    {
        return new Vec3(this.theEntity.posX, (double)this.getPathablePosY(), this.theEntity.posZ);
    }

    private int getPathablePosY()
    {
        if (this.theEntity.isInWater() && this.getCanSwim())
        {
            int pathableY = (int)this.theEntity.getEntityBoundingBox().minY;
            int entityX = MathHelper.floor_double(this.theEntity.posX);
            int entityZ = MathHelper.floor_double(this.theEntity.posZ);
            BlockPos.MutableBlockPos waterCheckPos = new BlockPos.MutableBlockPos(entityX, pathableY, entityZ);
            Block block = this.worldObj.getBlockState(waterCheckPos).getBlock();
            int waterDepth = 0;

            while (block == Blocks.flowing_water || block == Blocks.water)
            {
                ++pathableY;
                block = this.worldObj.getBlockState(waterCheckPos.set(entityX, pathableY, entityZ)).getBlock();
                ++waterDepth;

                if (waterDepth > 16)
                {
                    return (int)this.theEntity.getEntityBoundingBox().minY;
                }
            }

            return pathableY;
        }
        else
        {
            return (int)(this.theEntity.getEntityBoundingBox().minY + 0.5D);
        }
    }

    protected void removeSunnyPath()
    {
        super.removeSunnyPath();

        if (this.shouldAvoidSun)
        {
            BlockPos.MutableBlockPos skyCheckPos = new BlockPos.MutableBlockPos();

            if (this.worldObj.canSeeSky(skyCheckPos.set(MathHelper.floor_double(this.theEntity.posX), (int)(this.theEntity.getEntityBoundingBox().minY + 0.5D), MathHelper.floor_double(this.theEntity.posZ))))
            {
                return;
            }

            for (int pathIndex = 0; pathIndex < this.currentPath.getCurrentPathLength(); ++pathIndex)
            {
                PathPoint pathPoint = this.currentPath.getPathPointFromIndex(pathIndex);

                if (this.worldObj.canSeeSky(skyCheckPos.set(pathPoint.xCoord, pathPoint.yCoord, pathPoint.zCoord)))
                {
                    this.currentPath.setCurrentPathLength(pathIndex - 1);
                    return;
                }
            }
        }
    }

    protected boolean isDirectPathBetweenPoints(Vec3 startVec, Vec3 endVec, int sizeX, int sizeY, int sizeZ)
    {
        int currentX = MathHelper.floor_double(startVec.xCoord);
        int currentZ = MathHelper.floor_double(startVec.zCoord);
        double directionX = endVec.xCoord - startVec.xCoord;
        double directionZ = endVec.zCoord - startVec.zCoord;
        double distanceSquared = directionX * directionX + directionZ * directionZ;

        if (distanceSquared < 1.0E-8D)
        {
            return false;
        }
        else
        {
            double inverseDistance = MathHelper.fastInvSqrt(distanceSquared);
            directionX = directionX * inverseDistance;
            directionZ = directionZ * inverseDistance;
            sizeX = sizeX + 2;
            sizeZ = sizeZ + 2;

            if (!this.isSafeToStandAt(currentX, (int)startVec.yCoord, currentZ, sizeX, sizeY, sizeZ, startVec, directionX, directionZ))
            {
                return false;
            }
            else
            {
                sizeX = sizeX - 2;
                sizeZ = sizeZ - 2;
                double stepXDistance = 1.0D / Math.abs(directionX);
                double stepZDistance = 1.0D / Math.abs(directionZ);
                double nextXBoundary = (double)(currentX * 1) - startVec.xCoord;
                double nextZBoundary = (double)(currentZ * 1) - startVec.zCoord;

                if (directionX >= 0.0D)
                {
                    ++nextXBoundary;
                }

                if (directionZ >= 0.0D)
                {
                    ++nextZBoundary;
                }

                nextXBoundary = nextXBoundary / directionX;
                nextZBoundary = nextZBoundary / directionZ;
                int stepX = directionX < 0.0D ? -1 : 1;
                int stepZ = directionZ < 0.0D ? -1 : 1;
                int targetX = MathHelper.floor_double(endVec.xCoord);
                int targetZ = MathHelper.floor_double(endVec.zCoord);
                int remainingX = targetX - currentX;
                int remainingZ = targetZ - currentZ;

                while (remainingX * stepX > 0 || remainingZ * stepZ > 0)
                {
                    if (nextXBoundary < nextZBoundary)
                    {
                        nextXBoundary += stepXDistance;
                        currentX += stepX;
                        remainingX = targetX - currentX;
                    }
                    else
                    {
                        nextZBoundary += stepZDistance;
                        currentZ += stepZ;
                        remainingZ = targetZ - currentZ;
                    }

                    if (!this.isSafeToStandAt(currentX, (int)startVec.yCoord, currentZ, sizeX, sizeY, sizeZ, startVec, directionX, directionZ))
                    {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    private boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3 originVec, double directionX, double directionZ)
    {
        int minX = x - sizeX / 2;
        int minZ = z - sizeZ / 2;

        if (!this.isPositionClear(minX, y, minZ, sizeX, sizeY, sizeZ, originVec, directionX, directionZ))
        {
            return false;
        }
        else
        {
            BlockPos.MutableBlockPos groundCheckPos = new BlockPos.MutableBlockPos();

            for (int blockX = minX; blockX < minX + sizeX; ++blockX)
            {
                for (int blockZ = minZ; blockZ < minZ + sizeZ; ++blockZ)
                {
                    double relativeX = (double)blockX + 0.5D - originVec.xCoord;
                    double relativeZ = (double)blockZ + 0.5D - originVec.zCoord;

                    if (relativeX * directionX + relativeZ * directionZ >= 0.0D)
                    {
                        Block block = this.worldObj.getBlockState(groundCheckPos.set(blockX, y - 1, blockZ)).getBlock();
                        Material material = block.getMaterial();

                        if (material == Material.air)
                        {
                            return false;
                        }

                        if (material == Material.water && !this.theEntity.isInWater())
                        {
                            return false;
                        }

                        if (material == Material.lava)
                        {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    private boolean isPositionClear(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ, Vec3 originVec, double directionX, double directionZ)
    {
        for (BlockPos blockPos : BlockPos.getAllInBoxMutable(new BlockPos(minX, minY, minZ), new BlockPos(minX + sizeX - 1, minY + sizeY - 1, minZ + sizeZ - 1)))
        {
            double relativeX = (double)blockPos.getX() + 0.5D - originVec.xCoord;
            double relativeZ = (double)blockPos.getZ() + 0.5D - originVec.zCoord;

            if (relativeX * directionX + relativeZ * directionZ >= 0.0D)
            {
                Block block = this.worldObj.getBlockState(blockPos).getBlock();

                if (!block.isPassable(this.worldObj, blockPos))
                {
                    return false;
                }
            }
        }

        return true;
    }

    public void setAvoidsWater(boolean avoidsWater)
    {
        this.nodeProcessor.setAvoidsWater(avoidsWater);
    }

    public boolean getAvoidsWater()
    {
        return this.nodeProcessor.getAvoidsWater();
    }

    public void setBreakDoors(boolean canBreakDoors)
    {
        this.nodeProcessor.setBreakDoors(canBreakDoors);
    }

    public void setEnterDoors(boolean canEnterDoors)
    {
        this.nodeProcessor.setEnterDoors(canEnterDoors);
    }

    public boolean getEnterDoors()
    {
        return this.nodeProcessor.getEnterDoors();
    }

    public void setCanSwim(boolean canSwim)
    {
        this.nodeProcessor.setCanSwim(canSwim);
    }

    public boolean getCanSwim()
    {
        return this.nodeProcessor.getCanSwim();
    }

    public void setAvoidSun(boolean shouldAvoidSun)
    {
        this.shouldAvoidSun = shouldAvoidSun;
    }
}
