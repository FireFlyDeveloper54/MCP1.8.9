package net.minecraft.world;

import net.minecraft.util.BlockPos;

public class ChunkCoordIntPair
{
    public final int chunkXPos;
    public final int chunkZPos;
    private final int cachedHashCode;

    public ChunkCoordIntPair(int x, int z)
    {
        this.chunkXPos = x;
        this.chunkZPos = z;
        int xHash = 1664525 * x + 1013904223;
        int zHash = 1664525 * (z ^ -559038737) + 1013904223;
        this.cachedHashCode = xHash ^ zHash;
    }

    public static long chunkXZ2Int(int x, int z)
    {
        return (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
    }

    public int hashCode()
    {
        return this.cachedHashCode;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof ChunkCoordIntPair))
        {
            return false;
        }
        else
        {
            ChunkCoordIntPair chunkCoordIntPair = (ChunkCoordIntPair)other;
            return this.chunkXPos == chunkCoordIntPair.chunkXPos && this.chunkZPos == chunkCoordIntPair.chunkZPos;
        }
    }

    public int getCenterXPos()
    {
        return (this.chunkXPos << 4) + 8;
    }

    public int getCenterZPosition()
    {
        return (this.chunkZPos << 4) + 8;
    }

    public int getXStart()
    {
        return this.chunkXPos << 4;
    }

    public int getZStart()
    {
        return this.chunkZPos << 4;
    }

    public int getXEnd()
    {
        return (this.chunkXPos << 4) + 15;
    }

    public int getZEnd()
    {
        return (this.chunkZPos << 4) + 15;
    }

    public BlockPos getBlock(int x, int y, int z)
    {
        return new BlockPos((this.chunkXPos << 4) + x, y, (this.chunkZPos << 4) + z);
    }

    public BlockPos getCenterBlock(int y)
    {
        return new BlockPos(this.getCenterXPos(), y, this.getCenterZPosition());
    }

    public String toString()
    {
        return "[" + this.chunkXPos + ", " + this.chunkZPos + "]";
    }
}
