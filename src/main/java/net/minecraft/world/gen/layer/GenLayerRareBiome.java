package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;

public class GenLayerRareBiome extends GenLayer
{
    public GenLayerRareBiome(long baseSeedIn, GenLayer parent)
    {
        super(baseSeedIn);
        this.parent = parent;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int parentWidth = areaWidth + 2;
        int[] parentValues = this.parent.getInts(areaX - 1, areaY - 1, parentWidth, areaHeight + 2);
        int[] rareBiomeValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int localY = 0; localY < areaHeight; ++localY)
        {
            for (int localX = 0; localX < areaWidth; ++localX)
            {
                this.initChunkSeed((long)(localX + areaX), (long)(localY + areaY));
                int biomeId = parentValues[localX + 1 + (localY + 1) * parentWidth];

                if (this.nextInt(57) == 0)
                {
                    if (biomeId == BiomeGenBase.plains.biomeID)
                    {
                        rareBiomeValues[localX + localY * areaWidth] = BiomeGenBase.plains.biomeID + 128;
                    }
                    else
                    {
                        rareBiomeValues[localX + localY * areaWidth] = biomeId;
                    }
                }
                else
                {
                    rareBiomeValues[localX + localY * areaWidth] = biomeId;
                }
            }
        }

        return rareBiomeValues;
    }
}
