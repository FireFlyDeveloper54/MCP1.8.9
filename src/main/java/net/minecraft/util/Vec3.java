package net.minecraft.util;

public class Vec3
{
    public static final Vec3 ZERO = new Vec3(0.0D, 0.0D, 0.0D);
    public final double xCoord;
    public final double yCoord;
    public final double zCoord;

    public Vec3(double x, double y, double z)
    {
        if (x == -0.0D)
        {
            x = 0.0D;
        }

        if (y == -0.0D)
        {
            y = 0.0D;
        }

        if (z == -0.0D)
        {
            z = 0.0D;
        }

        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
    }

    public Vec3(Vec3i vector)
    {
        this((double)vector.getX(), (double)vector.getY(), (double)vector.getZ());
    }

    public Vec3 subtractReverse(Vec3 vec)
    {
        return new Vec3(vec.xCoord - this.xCoord, vec.yCoord - this.yCoord, vec.zCoord - this.zCoord);
    }

    public Vec3 normalize()
    {
        double length = (double)MathHelper.sqrt_double(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
        return length < 1.0E-4D ? ZERO : new Vec3(this.xCoord / length, this.yCoord / length, this.zCoord / length);
    }

    public double dotProduct(Vec3 vec)
    {
        return this.xCoord * vec.xCoord + this.yCoord * vec.yCoord + this.zCoord * vec.zCoord;
    }

    public Vec3 crossProduct(Vec3 vec)
    {
        return new Vec3(this.yCoord * vec.zCoord - this.zCoord * vec.yCoord, this.zCoord * vec.xCoord - this.xCoord * vec.zCoord, this.xCoord * vec.yCoord - this.yCoord * vec.xCoord);
    }

    public Vec3 subtract(Vec3 vec)
    {
        return this.subtract(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public Vec3 subtract(double x, double y, double z)
    {
        return this.addVector(-x, -y, -z);
    }

    public Vec3 add(Vec3 vec)
    {
        return this.addVector(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public Vec3 addVector(double x, double y, double z)
    {
        return new Vec3(this.xCoord + x, this.yCoord + y, this.zCoord + z);
    }

    public double distanceTo(Vec3 vec)
    {
        double deltaX = vec.xCoord - this.xCoord;
        double deltaY = vec.yCoord - this.yCoord;
        double deltaZ = vec.zCoord - this.zCoord;
        return (double)MathHelper.sqrt_double(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public double squareDistanceTo(Vec3 vec)
    {
        double deltaX = vec.xCoord - this.xCoord;
        double deltaY = vec.yCoord - this.yCoord;
        double deltaZ = vec.zCoord - this.zCoord;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public double lengthVector()
    {
        return (double)MathHelper.sqrt_double(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
    }

    public Vec3 getIntermediateWithXValue(Vec3 vec, double x)
    {
        double deltaX = vec.xCoord - this.xCoord;
        double deltaY = vec.yCoord - this.yCoord;
        double deltaZ = vec.zCoord - this.zCoord;

        if (deltaX * deltaX < 1.0000000116860974E-7D)
        {
            return null;
        }
        else
        {
            double interpolationFactor = (x - this.xCoord) / deltaX;
            return interpolationFactor >= 0.0D && interpolationFactor <= 1.0D ? new Vec3(this.xCoord + deltaX * interpolationFactor, this.yCoord + deltaY * interpolationFactor, this.zCoord + deltaZ * interpolationFactor) : null;
        }
    }

    public Vec3 getIntermediateWithYValue(Vec3 vec, double y)
    {
        double deltaX = vec.xCoord - this.xCoord;
        double deltaY = vec.yCoord - this.yCoord;
        double deltaZ = vec.zCoord - this.zCoord;

        if (deltaY * deltaY < 1.0000000116860974E-7D)
        {
            return null;
        }
        else
        {
            double interpolationFactor = (y - this.yCoord) / deltaY;
            return interpolationFactor >= 0.0D && interpolationFactor <= 1.0D ? new Vec3(this.xCoord + deltaX * interpolationFactor, this.yCoord + deltaY * interpolationFactor, this.zCoord + deltaZ * interpolationFactor) : null;
        }
    }

    public Vec3 getIntermediateWithZValue(Vec3 vec, double z)
    {
        double deltaX = vec.xCoord - this.xCoord;
        double deltaY = vec.yCoord - this.yCoord;
        double deltaZ = vec.zCoord - this.zCoord;

        if (deltaZ * deltaZ < 1.0000000116860974E-7D)
        {
            return null;
        }
        else
        {
            double interpolationFactor = (z - this.zCoord) / deltaZ;
            return interpolationFactor >= 0.0D && interpolationFactor <= 1.0D ? new Vec3(this.xCoord + deltaX * interpolationFactor, this.yCoord + deltaY * interpolationFactor, this.zCoord + deltaZ * interpolationFactor) : null;
        }
    }

    public String toString()
    {
        return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")";
    }

    public Vec3 rotatePitch(float pitch)
    {
        float[] sc = new float[2];
        MathHelper.sinCos(pitch, sc);
        float sin = sc[0];
        float cos = sc[1];
        double rotatedX = this.xCoord;
        double rotatedY = this.yCoord * (double)cos + this.zCoord * (double)sin;
        double rotatedZ = this.zCoord * (double)cos - this.yCoord * (double)sin;
        return new Vec3(rotatedX, rotatedY, rotatedZ);
    }

    public Vec3 rotateYaw(float yaw)
    {
        float[] sc = new float[2];
        MathHelper.sinCos(yaw, sc);
        float sin = sc[0];
        float cos = sc[1];
        double rotatedX = this.xCoord * (double)cos + this.zCoord * (double)sin;
        double rotatedY = this.yCoord;
        double rotatedZ = this.zCoord * (double)cos - this.xCoord * (double)sin;
        return new Vec3(rotatedX, rotatedY, rotatedZ);
    }
}
