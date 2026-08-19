package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;

public class GenLayerBiomeEdge extends GenLayer
{
    public GenLayerBiomeEdge(long baseSeedIn, GenLayer parent)
    {
        super(baseSeedIn);
        this.parent = parent;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] parentBiomes = this.parent.getInts(areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] outputBiomes = IntCache.getIntCache(areaWidth * areaHeight);

        for (int y = 0; y < areaHeight; ++y)
        {
            for (int x = 0; x < areaWidth; ++x)
            {
                this.initChunkSeed((long)(x + areaX), (long)(y + areaY));
                int centerBiomeId = parentBiomes[x + 1 + (y + 1) * (areaWidth + 2)];

                if (!this.replaceBiomeEdgeIfNecessary(parentBiomes, outputBiomes, x, y, areaWidth, centerBiomeId, BiomeGenBase.extremeHills.biomeID, BiomeGenBase.extremeHillsEdge.biomeID) && !this.replaceBiomeEdge(parentBiomes, outputBiomes, x, y, areaWidth, centerBiomeId, BiomeGenBase.mesaPlateau_F.biomeID, BiomeGenBase.mesa.biomeID) && !this.replaceBiomeEdge(parentBiomes, outputBiomes, x, y, areaWidth, centerBiomeId, BiomeGenBase.mesaPlateau.biomeID, BiomeGenBase.mesa.biomeID) && !this.replaceBiomeEdge(parentBiomes, outputBiomes, x, y, areaWidth, centerBiomeId, BiomeGenBase.megaTaiga.biomeID, BiomeGenBase.taiga.biomeID))
                {
                    if (centerBiomeId == BiomeGenBase.desert.biomeID)
                    {
                        int northBiomeId = parentBiomes[x + 1 + (y + 1 - 1) * (areaWidth + 2)];
                        int eastBiomeId = parentBiomes[x + 1 + 1 + (y + 1) * (areaWidth + 2)];
                        int westBiomeId = parentBiomes[x + 1 - 1 + (y + 1) * (areaWidth + 2)];
                        int southBiomeId = parentBiomes[x + 1 + (y + 1 + 1) * (areaWidth + 2)];

                        if (northBiomeId != BiomeGenBase.icePlains.biomeID && eastBiomeId != BiomeGenBase.icePlains.biomeID && westBiomeId != BiomeGenBase.icePlains.biomeID && southBiomeId != BiomeGenBase.icePlains.biomeID)
                        {
                            outputBiomes[x + y * areaWidth] = centerBiomeId;
                        }
                        else
                        {
                            outputBiomes[x + y * areaWidth] = BiomeGenBase.extremeHillsPlus.biomeID;
                        }
                    }
                    else if (centerBiomeId == BiomeGenBase.swampland.biomeID)
                    {
                        int northBiomeId = parentBiomes[x + 1 + (y + 1 - 1) * (areaWidth + 2)];
                        int eastBiomeId = parentBiomes[x + 1 + 1 + (y + 1) * (areaWidth + 2)];
                        int westBiomeId = parentBiomes[x + 1 - 1 + (y + 1) * (areaWidth + 2)];
                        int southBiomeId = parentBiomes[x + 1 + (y + 1 + 1) * (areaWidth + 2)];

                        if (northBiomeId != BiomeGenBase.desert.biomeID && eastBiomeId != BiomeGenBase.desert.biomeID && westBiomeId != BiomeGenBase.desert.biomeID && southBiomeId != BiomeGenBase.desert.biomeID && northBiomeId != BiomeGenBase.coldTaiga.biomeID && eastBiomeId != BiomeGenBase.coldTaiga.biomeID && westBiomeId != BiomeGenBase.coldTaiga.biomeID && southBiomeId != BiomeGenBase.coldTaiga.biomeID && northBiomeId != BiomeGenBase.icePlains.biomeID && eastBiomeId != BiomeGenBase.icePlains.biomeID && westBiomeId != BiomeGenBase.icePlains.biomeID && southBiomeId != BiomeGenBase.icePlains.biomeID)
                        {
                            if (northBiomeId != BiomeGenBase.jungle.biomeID && southBiomeId != BiomeGenBase.jungle.biomeID && eastBiomeId != BiomeGenBase.jungle.biomeID && westBiomeId != BiomeGenBase.jungle.biomeID)
                            {
                                outputBiomes[x + y * areaWidth] = centerBiomeId;
                            }
                            else
                            {
                                outputBiomes[x + y * areaWidth] = BiomeGenBase.jungleEdge.biomeID;
                            }
                        }
                        else
                        {
                            outputBiomes[x + y * areaWidth] = BiomeGenBase.plains.biomeID;
                        }
                    }
                    else
                    {
                        outputBiomes[x + y * areaWidth] = centerBiomeId;
                    }
                }
            }
        }

        return outputBiomes;
    }

    private boolean replaceBiomeEdgeIfNecessary(int[] parentBiomes, int[] outputBiomes, int x, int y, int areaWidth, int centerBiomeId, int targetBiomeId, int edgeBiomeId)
    {
        if (!biomesEqualOrMesaPlateau(centerBiomeId, targetBiomeId))
        {
            return false;
        }
        else
        {
            int northBiomeId = parentBiomes[x + 1 + (y + 1 - 1) * (areaWidth + 2)];
            int eastBiomeId = parentBiomes[x + 1 + 1 + (y + 1) * (areaWidth + 2)];
            int westBiomeId = parentBiomes[x + 1 - 1 + (y + 1) * (areaWidth + 2)];
            int southBiomeId = parentBiomes[x + 1 + (y + 1 + 1) * (areaWidth + 2)];

            if (this.canBiomesBeNeighbors(northBiomeId, targetBiomeId) && this.canBiomesBeNeighbors(eastBiomeId, targetBiomeId) && this.canBiomesBeNeighbors(westBiomeId, targetBiomeId) && this.canBiomesBeNeighbors(southBiomeId, targetBiomeId))
            {
                outputBiomes[x + y * areaWidth] = centerBiomeId;
            }
            else
            {
                outputBiomes[x + y * areaWidth] = edgeBiomeId;
            }

            return true;
        }
    }

    private boolean replaceBiomeEdge(int[] parentBiomes, int[] outputBiomes, int x, int y, int areaWidth, int centerBiomeId, int targetBiomeId, int edgeBiomeId)
    {
        if (centerBiomeId != targetBiomeId)
        {
            return false;
        }
        else
        {
            int northBiomeId = parentBiomes[x + 1 + (y + 1 - 1) * (areaWidth + 2)];
            int eastBiomeId = parentBiomes[x + 1 + 1 + (y + 1) * (areaWidth + 2)];
            int westBiomeId = parentBiomes[x + 1 - 1 + (y + 1) * (areaWidth + 2)];
            int southBiomeId = parentBiomes[x + 1 + (y + 1 + 1) * (areaWidth + 2)];

            if (biomesEqualOrMesaPlateau(northBiomeId, targetBiomeId) && biomesEqualOrMesaPlateau(eastBiomeId, targetBiomeId) && biomesEqualOrMesaPlateau(westBiomeId, targetBiomeId) && biomesEqualOrMesaPlateau(southBiomeId, targetBiomeId))
            {
                outputBiomes[x + y * areaWidth] = centerBiomeId;
            }
            else
            {
                outputBiomes[x + y * areaWidth] = edgeBiomeId;
            }

            return true;
        }
    }

    private boolean canBiomesBeNeighbors(int biomeId, int neighborBiomeId)
    {
        if (biomesEqualOrMesaPlateau(biomeId, neighborBiomeId))
        {
            return true;
        }
        else
        {
            BiomeGenBase biomeGenBase = BiomeGenBase.getBiome(biomeId);
            BiomeGenBase neighborBiome = BiomeGenBase.getBiome(neighborBiomeId);

            if (biomeGenBase != null && neighborBiome != null)
            {
                BiomeGenBase.TempCategory biomeTemperature = biomeGenBase.getTempCategory();
                BiomeGenBase.TempCategory neighborTemperature = neighborBiome.getTempCategory();
                return biomeTemperature == neighborTemperature || biomeTemperature == BiomeGenBase.TempCategory.MEDIUM || neighborTemperature == BiomeGenBase.TempCategory.MEDIUM;
            }
            else
            {
                return false;
            }
        }
    }
}
