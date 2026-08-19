package net.minecraft.world.gen.layer;

public class GenLayerSmooth extends GenLayer
{
    public GenLayerSmooth(long baseSeedIn, GenLayer parent)
    {
        super(baseSeedIn);
        super.parent = parent;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int parentAreaX = areaX - 1;
        int parentAreaY = areaY - 1;
        int parentWidth = areaWidth + 2;
        int parentHeight = areaHeight + 2;
        int[] parentValues = this.parent.getInts(parentAreaX, parentAreaY, parentWidth, parentHeight);
        int[] smoothedValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int localY = 0; localY < areaHeight; ++localY)
        {
            for (int localX = 0; localX < areaWidth; ++localX)
            {
                int westBiome = parentValues[localX + (localY + 1) * parentWidth];
                int eastBiome = parentValues[localX + 2 + (localY + 1) * parentWidth];
                int northBiome = parentValues[localX + 1 + localY * parentWidth];
                int southBiome = parentValues[localX + 1 + (localY + 2) * parentWidth];
                int centerBiome = parentValues[localX + 1 + (localY + 1) * parentWidth];

                if (westBiome == eastBiome && northBiome == southBiome)
                {
                    this.initChunkSeed((long)(localX + areaX), (long)(localY + areaY));

                    if (this.nextInt(2) == 0)
                    {
                        centerBiome = westBiome;
                    }
                    else
                    {
                        centerBiome = northBiome;
                    }
                }
                else
                {
                    if (westBiome == eastBiome)
                    {
                        centerBiome = westBiome;
                    }

                    if (northBiome == southBiome)
                    {
                        centerBiome = northBiome;
                    }
                }

                smoothedValues[localX + localY * areaWidth] = centerBiome;
            }
        }

        return smoothedValues;
    }
}
