package net.minecraft.util;

import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;

public class Rotations
{
    protected final float x;
    protected final float y;
    protected final float z;

    public Rotations(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Rotations(NBTTagList nbt)
    {
        this.x = nbt.getFloatAt(0);
        this.y = nbt.getFloatAt(1);
        this.z = nbt.getFloatAt(2);
    }

    public NBTTagList writeToNBT()
    {
        NBTTagList nBTTagList = new NBTTagList();
        nBTTagList.appendTag(new NBTTagFloat(this.x));
        nBTTagList.appendTag(new NBTTagFloat(this.y));
        nBTTagList.appendTag(new NBTTagFloat(this.z));
        return nBTTagList;
    }

    public boolean equals(Object other)
    {
        if (!(other instanceof Rotations))
        {
            return false;
        }
        else
        {
            Rotations rotations = (Rotations)other;
            return this.x == rotations.x && this.y == rotations.y && this.z == rotations.z;
        }
    }

    public float getX()
    {
        return this.x;
    }

    public float getY()
    {
        return this.y;
    }

    public float getZ()
    {
        return this.z;
    }
}
