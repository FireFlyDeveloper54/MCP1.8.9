package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;

public class GenLayerDeepOcean extends GenLayer
{
    public GenLayerDeepOcean(long baseSeedIn, GenLayer parent)
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
        int[] oceanValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int localY = 0; localY < areaHeight; ++localY)
        {
            for (int localX = 0; localX < areaWidth; ++localX)
            {
                int northBiome = parentValues[localX + 1 + localY * parentWidth];
                int eastBiome = parentValues[localX + 2 + (localY + 1) * parentWidth];
                int westBiome = parentValues[localX + (localY + 1) * parentWidth];
                int southBiome = parentValues[localX + 1 + (localY + 2) * parentWidth];
                int centerBiome = parentValues[localX + 1 + (localY + 1) * parentWidth];
                int adjacentOceanCount = 0;

                if (northBiome == 0)
                {
                    ++adjacentOceanCount;
                }

                if (eastBiome == 0)
                {
                    ++adjacentOceanCount;
                }

                if (westBiome == 0)
                {
                    ++adjacentOceanCount;
                }

                if (southBiome == 0)
                {
                    ++adjacentOceanCount;
                }

                if (centerBiome == 0 && adjacentOceanCount > 3)
                {
                    oceanValues[localX + localY * areaWidth] = BiomeGenBase.deepOcean.biomeID;
                }
                else
                {
                    oceanValues[localX + localY * areaWidth] = centerBiome;
                }
            }
        }

        return oceanValues;
    }
}
