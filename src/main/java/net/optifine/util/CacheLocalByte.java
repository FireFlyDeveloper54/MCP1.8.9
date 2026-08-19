package net.optifine.util;

import java.util.Arrays;

public class CacheLocalByte
{
    private int sizeX = 18;
    private int sizeY = 128;
    private int sizeZ = 18;
    private int offsetX = 0;
    private int offsetY = 0;
    private int offsetZ = 0;
    private byte[][][] cache = (byte[][][])null;
    private byte[] cachedRow = null;
    private int cachedZIndex = 0;

    public CacheLocalByte(int maxX, int maxY, int maxZ)
    {
        this.sizeX = maxX;
        this.sizeY = maxY;
        this.sizeZ = maxZ;
        this.cache = new byte[maxX][maxY][maxZ];
        this.resetCache();
    }

    public void resetCache()
    {
        for (int x = 0; x < this.sizeX; ++x)
        {
            byte[][] rows = this.cache[x];

            for (int y = 0; y < this.sizeY; ++y)
            {
                Arrays.fill(rows[y], (byte)-1);
            }
        }
    }

    public void setOffset(int x, int y, int z)
    {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        this.resetCache();
    }

    public byte get(int x, int y, int z)
    {
        try
        {
            this.cachedRow = this.cache[x - this.offsetX][y - this.offsetY];
            this.cachedZIndex = z - this.offsetZ;
            return this.cachedRow[this.cachedZIndex];
        }
        catch (ArrayIndexOutOfBoundsException exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            return (byte)-1;
        }
    }

    public void setLast(byte val)
    {
        try
        {
            this.cachedRow[this.cachedZIndex] = val;
        }
        catch (Exception exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
        }
    }
}
