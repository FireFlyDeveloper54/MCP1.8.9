package net.minecraft.util;

public class AxisAlignedBB
{
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AxisAlignedBB(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public AxisAlignedBB(BlockPos pos1, BlockPos pos2)
    {
        this.minX = (double)pos1.getX();
        this.minY = (double)pos1.getY();
        this.minZ = (double)pos1.getZ();
        this.maxX = (double)pos2.getX();
        this.maxY = (double)pos2.getY();
        this.maxZ = (double)pos2.getZ();
    }

    public AxisAlignedBB addCoord(double x, double y, double z)
    {
        double newMinX = this.minX;
        double newMinY = this.minY;
        double newMinZ = this.minZ;
        double newMaxX = this.maxX;
        double newMaxY = this.maxY;
        double newMaxZ = this.maxZ;

        if (x < 0.0D)
        {
            newMinX += x;
        }
        else if (x > 0.0D)
        {
            newMaxX += x;
        }

        if (y < 0.0D)
        {
            newMinY += y;
        }
        else if (y > 0.0D)
        {
            newMaxY += y;
        }

        if (z < 0.0D)
        {
            newMinZ += z;
        }
        else if (z > 0.0D)
        {
            newMaxZ += z;
        }

        return new AxisAlignedBB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    public AxisAlignedBB expand(double x, double y, double z)
    {
        double expandedMinX = this.minX - x;
        double expandedMinY = this.minY - y;
        double expandedMinZ = this.minZ - z;
        double expandedMaxX = this.maxX + x;
        double expandedMaxY = this.maxY + y;
        double expandedMaxZ = this.maxZ + z;
        return new AxisAlignedBB(expandedMinX, expandedMinY, expandedMinZ, expandedMaxX, expandedMaxY, expandedMaxZ);
    }

    public AxisAlignedBB union(AxisAlignedBB other)
    {
        double unionMinX = Math.min(this.minX, other.minX);
        double unionMinY = Math.min(this.minY, other.minY);
        double unionMinZ = Math.min(this.minZ, other.minZ);
        double unionMaxX = Math.max(this.maxX, other.maxX);
        double unionMaxY = Math.max(this.maxY, other.maxY);
        double unionMaxZ = Math.max(this.maxZ, other.maxZ);
        return new AxisAlignedBB(unionMinX, unionMinY, unionMinZ, unionMaxX, unionMaxY, unionMaxZ);
    }

    public static AxisAlignedBB fromBounds(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxX = Math.max(x1, x2);
        double maxY = Math.max(y1, y2);
        double maxZ = Math.max(z1, z2);
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AxisAlignedBB offset(double x, double y, double z)
    {
        return new AxisAlignedBB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public double calculateXOffset(AxisAlignedBB other, double offsetX)
    {
        if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ)
        {
            if (offsetX > 0.0D && other.maxX <= this.minX)
            {
                double candidateOffset = this.minX - other.maxX;

                if (candidateOffset < offsetX)
                {
                    offsetX = candidateOffset;
                }
            }
            else if (offsetX < 0.0D && other.minX >= this.maxX)
            {
                double candidateOffset = this.maxX - other.minX;

                if (candidateOffset > offsetX)
                {
                    offsetX = candidateOffset;
                }
            }

            return offsetX;
        }
        else
        {
            return offsetX;
        }
    }

    public double calculateYOffset(AxisAlignedBB other, double offsetY)
    {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ)
        {
            if (offsetY > 0.0D && other.maxY <= this.minY)
            {
                double candidateOffset = this.minY - other.maxY;

                if (candidateOffset < offsetY)
                {
                    offsetY = candidateOffset;
                }
            }
            else if (offsetY < 0.0D && other.minY >= this.maxY)
            {
                double candidateOffset = this.maxY - other.minY;

                if (candidateOffset > offsetY)
                {
                    offsetY = candidateOffset;
                }
            }

            return offsetY;
        }
        else
        {
            return offsetY;
        }
    }

    public double calculateZOffset(AxisAlignedBB other, double offsetZ)
    {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY)
        {
            if (offsetZ > 0.0D && other.maxZ <= this.minZ)
            {
                double candidateOffset = this.minZ - other.maxZ;

                if (candidateOffset < offsetZ)
                {
                    offsetZ = candidateOffset;
                }
            }
            else if (offsetZ < 0.0D && other.minZ >= this.maxZ)
            {
                double candidateOffset = this.maxZ - other.minZ;

                if (candidateOffset > offsetZ)
                {
                    offsetZ = candidateOffset;
                }
            }

            return offsetZ;
        }
        else
        {
            return offsetZ;
        }
    }

    public boolean intersectsWith(AxisAlignedBB other)
    {
        return other.maxX > this.minX && other.minX < this.maxX ? (other.maxY > this.minY && other.minY < this.maxY ? other.maxZ > this.minZ && other.minZ < this.maxZ : false) : false;
    }

    public boolean isVecInside(Vec3 vec)
    {
        return vec.xCoord > this.minX && vec.xCoord < this.maxX ? (vec.yCoord > this.minY && vec.yCoord < this.maxY ? vec.zCoord > this.minZ && vec.zCoord < this.maxZ : false) : false;
    }

    public double getAverageEdgeLength()
    {
        double xLength = this.maxX - this.minX;
        double yLength = this.maxY - this.minY;
        double zLength = this.maxZ - this.minZ;
        return (xLength + yLength + zLength) / 3.0D;
    }

    public AxisAlignedBB contract(double x, double y, double z)
    {
        double contractedMinX = this.minX + x;
        double contractedMinY = this.minY + y;
        double contractedMinZ = this.minZ + z;
        double contractedMaxX = this.maxX - x;
        double contractedMaxY = this.maxY - y;
        double contractedMaxZ = this.maxZ - z;
        return new AxisAlignedBB(contractedMinX, contractedMinY, contractedMinZ, contractedMaxX, contractedMaxY, contractedMaxZ);
    }

    public MovingObjectPosition calculateIntercept(Vec3 start, Vec3 end)
    {
        Vec3 minXIntersection = start.getIntermediateWithXValue(end, this.minX);
        Vec3 maxXIntersection = start.getIntermediateWithXValue(end, this.maxX);
        Vec3 minYIntersection = start.getIntermediateWithYValue(end, this.minY);
        Vec3 maxYIntersection = start.getIntermediateWithYValue(end, this.maxY);
        Vec3 minZIntersection = start.getIntermediateWithZValue(end, this.minZ);
        Vec3 maxZIntersection = start.getIntermediateWithZValue(end, this.maxZ);

        if (!this.isVecInYZ(minXIntersection))
        {
            minXIntersection = null;
        }

        if (!this.isVecInYZ(maxXIntersection))
        {
            maxXIntersection = null;
        }

        if (!this.isVecInXZ(minYIntersection))
        {
            minYIntersection = null;
        }

        if (!this.isVecInXZ(maxYIntersection))
        {
            maxYIntersection = null;
        }

        if (!this.isVecInXY(minZIntersection))
        {
            minZIntersection = null;
        }

        if (!this.isVecInXY(maxZIntersection))
        {
            maxZIntersection = null;
        }

        Vec3 closestIntersection = null;

        if (minXIntersection != null)
        {
            closestIntersection = minXIntersection;
        }

        if (maxXIntersection != null && (closestIntersection == null || start.squareDistanceTo(maxXIntersection) < start.squareDistanceTo(closestIntersection)))
        {
            closestIntersection = maxXIntersection;
        }

        if (minYIntersection != null && (closestIntersection == null || start.squareDistanceTo(minYIntersection) < start.squareDistanceTo(closestIntersection)))
        {
            closestIntersection = minYIntersection;
        }

        if (maxYIntersection != null && (closestIntersection == null || start.squareDistanceTo(maxYIntersection) < start.squareDistanceTo(closestIntersection)))
        {
            closestIntersection = maxYIntersection;
        }

        if (minZIntersection != null && (closestIntersection == null || start.squareDistanceTo(minZIntersection) < start.squareDistanceTo(closestIntersection)))
        {
            closestIntersection = minZIntersection;
        }

        if (maxZIntersection != null && (closestIntersection == null || start.squareDistanceTo(maxZIntersection) < start.squareDistanceTo(closestIntersection)))
        {
            closestIntersection = maxZIntersection;
        }

        if (closestIntersection == null)
        {
            return null;
        }
        else
        {
            EnumFacing hitSide = null;

            if (closestIntersection == minXIntersection)
            {
                hitSide = EnumFacing.WEST;
            }
            else if (closestIntersection == maxXIntersection)
            {
                hitSide = EnumFacing.EAST;
            }
            else if (closestIntersection == minYIntersection)
            {
                hitSide = EnumFacing.DOWN;
            }
            else if (closestIntersection == maxYIntersection)
            {
                hitSide = EnumFacing.UP;
            }
            else if (closestIntersection == minZIntersection)
            {
                hitSide = EnumFacing.NORTH;
            }
            else
            {
                hitSide = EnumFacing.SOUTH;
            }

            return new MovingObjectPosition(closestIntersection, hitSide);
        }
    }

    private boolean isVecInYZ(Vec3 vec)
    {
        return vec == null ? false : vec.yCoord >= this.minY && vec.yCoord <= this.maxY && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
    }

    private boolean isVecInXZ(Vec3 vec)
    {
        return vec == null ? false : vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
    }

    private boolean isVecInXY(Vec3 vec)
    {
        return vec == null ? false : vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.yCoord >= this.minY && vec.yCoord <= this.maxY;
    }

    public String toString()
    {
        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    public boolean hasNaN()
    {
        return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
    }
}
