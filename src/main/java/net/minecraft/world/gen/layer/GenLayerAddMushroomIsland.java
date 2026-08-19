package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;

public class GenLayerAddMushroomIsland extends GenLayer
{
    public GenLayerAddMushroomIsland(long baseSeedIn, GenLayer parent)
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
        int[] mushroomIslandValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int localY = 0; localY < areaHeight; ++localY)
        {
            for (int localX = 0; localX < areaWidth; ++localX)
            {
                int northwestBiome = parentValues[localX + localY * parentWidth];
                int northeastBiome = parentValues[localX + 2 + localY * parentWidth];
                int southwestBiome = parentValues[localX + (localY + 2) * parentWidth];
                int southeastBiome = parentValues[localX + 2 + (localY + 2) * parentWidth];
                int centerBiome = parentValues[localX + 1 + (localY + 1) * parentWidth];
                this.initChunkSeed((long)(localX + areaX), (long)(localY + areaY));

                if (centerBiome == 0 && northwestBiome == 0 && northeastBiome == 0 && southwestBiome == 0 && southeastBiome == 0 && this.nextInt(100) == 0)
                {
                    mushroomIslandValues[localX + localY * areaWidth] = BiomeGenBase.mushroomIsland.biomeID;
                }
                else
                {
                    mushroomIslandValues[localX + localY * areaWidth] = centerBiome;
                }
            }
        }

        return mushroomIslandValues;
    }
}
