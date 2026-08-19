package net.minecraft.world.gen.layer;

public class GenLayerRemoveTooMuchOcean extends GenLayer
{
    public GenLayerRemoveTooMuchOcean(long baseSeedIn, GenLayer parent)
    {
        super(baseSeedIn);
        this.parent = parent;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int parentAreaX = areaX - 1;
        int parentAreaY = areaY - 1;
        int parentWidth = areaWidth + 2;
        int parentHeight = areaHeight + 2;
        int[] parentValues = this.parent.getInts(parentAreaX, parentAreaY, parentWidth, parentHeight);
        int[] reducedOceanValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int localY = 0; localY < areaHeight; ++localY)
        {
            for (int localX = 0; localX < areaWidth; ++localX)
            {
                int northBiome = parentValues[localX + 1 + localY * parentWidth];
                int eastBiome = parentValues[localX + 2 + (localY + 1) * parentWidth];
                int westBiome = parentValues[localX + (localY + 1) * parentWidth];
                int southBiome = parentValues[localX + 1 + (localY + 2) * parentWidth];
                int centerBiome = parentValues[localX + 1 + (localY + 1) * parentWidth];
                reducedOceanValues[localX + localY * areaWidth] = centerBiome;
                this.initChunkSeed((long)(localX + areaX), (long)(localY + areaY));

                if (centerBiome == 0 && northBiome == 0 && eastBiome == 0 && westBiome == 0 && southBiome == 0 && this.nextInt(2) == 0)
                {
                    reducedOceanValues[localX + localY * areaWidth] = 1;
                }
            }
        }

        return reducedOceanValues;
    }
}
