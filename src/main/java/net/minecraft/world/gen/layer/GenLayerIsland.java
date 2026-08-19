package net.minecraft.world.gen.layer;

public class GenLayerIsland extends GenLayer
{
    public GenLayerIsland(long baseSeedIn)
    {
        super(baseSeedIn);
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] islandValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int localY = 0; localY < areaHeight; ++localY)
        {
            for (int localX = 0; localX < areaWidth; ++localX)
            {
                this.initChunkSeed((long)(areaX + localX), (long)(areaY + localY));
                islandValues[localX + localY * areaWidth] = this.nextInt(10) == 0 ? 1 : 0;
            }
        }

        if (areaX > -areaWidth && areaX <= 0 && areaY > -areaHeight && areaY <= 0)
        {
            islandValues[-areaX + -areaY * areaWidth] = 1;
        }

        return islandValues;
    }
}
