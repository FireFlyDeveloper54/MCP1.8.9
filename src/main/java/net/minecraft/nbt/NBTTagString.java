package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagString extends NBTBase
{
    private String data;
    private String stringCache;

    public NBTTagString()
    {
        this.data = "";
    }

    public NBTTagString(String data)
    {
        this.data = data;

        if (data == null)
        {
            throw new IllegalArgumentException("Empty string not allowed");
        }
    }

    void write(DataOutput output) throws IOException
    {
        output.writeUTF(this.data);
    }

    void read(DataInput input, int depth, NBTSizeTracker sizeTracker) throws IOException
    {
        this.stringCache = null;
        sizeTracker.read(288L);
        this.data = input.readUTF();
        sizeTracker.read((long)(16 * this.data.length()));
    }

    public byte getId()
    {
        return (byte)8;
    }

    public String toString()
    {
        if (this.stringCache == null)
        {
            this.stringCache = "\"" + this.data.replace("\"", "\\\"") + "\"";
        }

        return this.stringCache;
    }

    public NBTBase copy()
    {
        return new NBTTagString(this.data);
    }

    public boolean hasNoTags()
    {
        return this.data.isEmpty();
    }

    public boolean equals(Object other)
    {
        if (!super.equals(other))
        {
            return false;
        }
        else
        {
            NBTTagString nBTTagString = (NBTTagString)other;
            return this.data == null && nBTTagString.data == null || this.data != null && this.data.equals(nBTTagString.data);
        }
    }

    public int hashCode()
    {
        return super.hashCode() ^ this.data.hashCode();
    }

    public String getString()
    {
        return this.data;
    }
}
