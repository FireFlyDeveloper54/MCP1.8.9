package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;

public class GenLayerRiverMix extends GenLayer
{
    private GenLayer biomePatternGeneratorChain;
    private GenLayer riverPatternGeneratorChain;

    public GenLayerRiverMix(long baseSeedIn, GenLayer biomePatternGeneratorChainIn, GenLayer riverPatternGeneratorChainIn)
    {
        super(baseSeedIn);
        this.biomePatternGeneratorChain = biomePatternGeneratorChainIn;
        this.riverPatternGeneratorChain = riverPatternGeneratorChainIn;
    }

    public void initWorldGenSeed(long seed)
    {
        this.biomePatternGeneratorChain.initWorldGenSeed(seed);
        this.riverPatternGeneratorChain.initWorldGenSeed(seed);
        super.initWorldGenSeed(seed);
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] biomeValues = this.biomePatternGeneratorChain.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] riverValues = this.riverPatternGeneratorChain.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] mixedValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int index = 0; index < areaWidth * areaHeight; ++index)
        {
            if (biomeValues[index] != BiomeGenBase.ocean.biomeID && biomeValues[index] != BiomeGenBase.deepOcean.biomeID)
            {
                if (riverValues[index] == BiomeGenBase.river.biomeID)
                {
                    if (biomeValues[index] == BiomeGenBase.icePlains.biomeID)
                    {
                        mixedValues[index] = BiomeGenBase.frozenRiver.biomeID;
                    }
                    else if (biomeValues[index] != BiomeGenBase.mushroomIsland.biomeID && biomeValues[index] != BiomeGenBase.mushroomIslandShore.biomeID)
                    {
                        mixedValues[index] = riverValues[index] & 255;
                    }
                    else
                    {
                        mixedValues[index] = BiomeGenBase.mushroomIslandShore.biomeID;
                    }
                }
                else
                {
                    mixedValues[index] = biomeValues[index];
                }
            }
            else
            {
                mixedValues[index] = biomeValues[index];
            }
        }

        return mixedValues;
    }
}
