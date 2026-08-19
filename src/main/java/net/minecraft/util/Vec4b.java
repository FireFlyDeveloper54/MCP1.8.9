package net.minecraft.util;

public class Vec4b
{
    private byte x;
    private byte y;
    private byte z;
    private byte w;

    public Vec4b(byte xIn, byte yIn, byte zIn, byte wIn)
    {
        this.x = xIn;
        this.y = yIn;
        this.z = zIn;
        this.w = wIn;
    }

    public Vec4b(Vec4b other)
    {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
    }

    public byte getX()
    {
        return this.x;
    }

    public byte getY()
    {
        return this.y;
    }

    public byte getZ()
    {
        return this.z;
    }

    public byte getW()
    {
        return this.w;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof Vec4b))
        {
            return false;
        }
        else
        {
            Vec4b vec4b = (Vec4b)other;
            return this.x != vec4b.x ? false : (this.w != vec4b.w ? false : (this.y != vec4b.y ? false : this.z == vec4b.z));
        }
    }

    public int hashCode()
    {
        int hash = this.x;
        hash = 31 * hash + this.y;
        hash = 31 * hash + this.z;
        hash = 31 * hash + this.w;
        return hash;
    }
}
