package net.minecraft.world.gen.layer;

public class GenLayerRiverInit extends GenLayer
{
    public GenLayerRiverInit(long baseSeedIn, GenLayer parent)
    {
        super(baseSeedIn);
        this.parent = parent;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] parentValues = this.parent.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] riverSeeds = IntCache.getIntCache(areaWidth * areaHeight);

        for (int localY = 0; localY < areaHeight; ++localY)
        {
            for (int localX = 0; localX < areaWidth; ++localX)
            {
                this.initChunkSeed((long)(localX + areaX), (long)(localY + areaY));
                riverSeeds[localX + localY * areaWidth] = parentValues[localX + localY * areaWidth] > 0 ? this.nextInt(299999) + 2 : 0;
            }
        }

        return riverSeeds;
    }
}
