package net.minecraft.util;

import com.google.common.base.MoreObjects;

public class Vec3i implements Comparable<Vec3i>
{
    public static final Vec3i NULL_VECTOR = new Vec3i(0, 0, 0);
    private final int x;
    private final int y;
    private final int z;

    public Vec3i(int xIn, int yIn, int zIn)
    {
        this.x = xIn;
        this.y = yIn;
        this.z = zIn;
    }

    public Vec3i(double xIn, double yIn, double zIn)
    {
        this(MathHelper.floor_double(xIn), MathHelper.floor_double(yIn), MathHelper.floor_double(zIn));
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof Vec3i))
        {
            return false;
        }
        else
        {
            Vec3i vec3i = (Vec3i)other;
            return this.getX() != vec3i.getX() ? false : (this.getY() != vec3i.getY() ? false : this.getZ() == vec3i.getZ());
        }
    }

    public int hashCode()
    {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    public int compareTo(Vec3i other)
    {
        return this.getY() == other.getY() ? (this.getZ() == other.getZ() ? this.getX() - other.getX() : this.getZ() - other.getZ()) : this.getY() - other.getY();
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getZ()
    {
        return this.z;
    }

    public Vec3i crossProduct(Vec3i vec)
    {
        return new Vec3i(this.getY() * vec.getZ() - this.getZ() * vec.getY(), this.getZ() * vec.getX() - this.getX() * vec.getZ(), this.getX() * vec.getY() - this.getY() * vec.getX());
    }

    public double distanceSq(double toX, double toY, double toZ)
    {
        double xCoordinate = (double)this.getX() - toX;
        double yCoordinate = (double)this.getY() - toY;
        double zCoordinate = (double)this.getZ() - toZ;
        return xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate;
    }

    public double distanceSqToCenter(double xIn, double yIn, double zIn)
    {
        double xCoordinate = (double)this.getX() + 0.5D - xIn;
        double yCoordinate = (double)this.getY() + 0.5D - yIn;
        double zCoordinate = (double)this.getZ() + 0.5D - zIn;
        return xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate;
    }

    public double distanceSq(Vec3i to)
    {
        return this.distanceSq((double)to.getX(), (double)to.getY(), (double)to.getZ());
    }

    public String toString()
    {
        return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }
}
