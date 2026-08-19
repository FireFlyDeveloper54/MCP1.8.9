package net.optifine.util;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.MathHelper;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class RenderChunkUtils
{
    public static int getCountBlocks(RenderChunk renderChunk)
    {
        ExtendedBlockStorage[] blockStorages = renderChunk.getChunk().getBlockStorageArray();

        if (blockStorages == null)
        {
            return 0;
        }
        else
        {
            int sectionIndex = renderChunk.getPosition().getY() >> 4;
            ExtendedBlockStorage blockStorage = blockStorages[sectionIndex];
            return blockStorage == null ? 0 : blockStorage.getBlockRefCount();
        }
    }

    public static double getRelativeBufferSize(RenderChunk renderChunk)
    {
        int blockCount = getCountBlocks(renderChunk);
        double relativeBufferSize = getRelativeBufferSize(blockCount);
        return relativeBufferSize;
    }

    public static double getRelativeBufferSize(int blockCount)
    {
        double normalizedBlockCount = (double)blockCount / 4096.0D;
        normalizedBlockCount = normalizedBlockCount * 0.995D;
        double normalizedRange = normalizedBlockCount * 2.0D - 1.0D;
        normalizedRange = MathHelper.clamp_double(normalizedRange, -1.0D, 1.0D);
        return (double)MathHelper.sqrt_double(1.0D - normalizedRange * normalizedRange);
    }
}
