package net.minecraft.pathfinding;

import net.minecraft.util.MathHelper;

public class PathPoint
{
    public final int xCoord;
    public final int yCoord;
    public final int zCoord;
    private final int hash;
    int index = -1;
    float totalPathDistance;
    float distanceToNext;
    float distanceToTarget;
    PathPoint previous;
    public boolean visited;

    public PathPoint(int x, int y, int z)
    {
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
        this.hash = makeHash(x, y, z);
    }

    public static int makeHash(int x, int y, int z)
    {
        return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? 32768 : 0);
    }

    public float distanceTo(PathPoint pathpointIn)
    {
        float deltaX = (float)(pathpointIn.xCoord - this.xCoord);
        float deltaY = (float)(pathpointIn.yCoord - this.yCoord);
        float deltaZ = (float)(pathpointIn.zCoord - this.zCoord);
        return MathHelper.sqrt_float(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public float distanceToSquared(PathPoint pathpointIn)
    {
        float deltaX = (float)(pathpointIn.xCoord - this.xCoord);
        float deltaY = (float)(pathpointIn.yCoord - this.yCoord);
        float deltaZ = (float)(pathpointIn.zCoord - this.zCoord);
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public boolean equals(Object object)
    {
        if (!(object instanceof PathPoint))
        {
            return false;
        }
        else
        {
            PathPoint otherPoint = (PathPoint)object;
            return this.hash == otherPoint.hash && this.xCoord == otherPoint.xCoord && this.yCoord == otherPoint.yCoord && this.zCoord == otherPoint.zCoord;
        }
    }

    public int hashCode()
    {
        return this.hash;
    }

    public boolean isAssigned()
    {
        return this.index >= 0;
    }

    public String toString()
    {
        return this.xCoord + ", " + this.yCoord + ", " + this.zCoord;
    }
}
