package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;

public class GenLayerRiver extends GenLayer
{
    public GenLayerRiver(long baseSeedIn, GenLayer parent)
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
        int[] riverValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int localY = 0; localY < areaHeight; ++localY)
        {
            for (int localX = 0; localX < areaWidth; ++localX)
            {
                int westRiverId = this.riverFilter(parentValues[localX + (localY + 1) * parentWidth]);
                int eastRiverId = this.riverFilter(parentValues[localX + 2 + (localY + 1) * parentWidth]);
                int northRiverId = this.riverFilter(parentValues[localX + 1 + localY * parentWidth]);
                int southRiverId = this.riverFilter(parentValues[localX + 1 + (localY + 2) * parentWidth]);
                int centerRiverId = this.riverFilter(parentValues[localX + 1 + (localY + 1) * parentWidth]);

                if (centerRiverId == westRiverId && centerRiverId == northRiverId && centerRiverId == eastRiverId && centerRiverId == southRiverId)
                {
                    riverValues[localX + localY * areaWidth] = -1;
                }
                else
                {
                    riverValues[localX + localY * areaWidth] = BiomeGenBase.river.biomeID;
                }
            }
        }

        return riverValues;
    }

    private int riverFilter(int biomeId)
    {
        return biomeId >= 2 ? 2 + (biomeId & 1) : biomeId;
    }
}
