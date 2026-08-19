package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenJungle;
import net.minecraft.world.biome.BiomeGenMesa;

public class GenLayerShore extends GenLayer
{
    public GenLayerShore(long baseSeedIn, GenLayer parent)
    {
        super(baseSeedIn);
        this.parent = parent;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int parentWidth = areaWidth + 2;
        int[] parentBiomes = this.parent.getInts(areaX - 1, areaY - 1, parentWidth, areaHeight + 2);
        int[] shoreBiomes = IntCache.getIntCache(areaWidth * areaHeight);

        for (int localY = 0; localY < areaHeight; ++localY)
        {
            for (int localX = 0; localX < areaWidth; ++localX)
            {
                this.initChunkSeed((long)(localX + areaX), (long)(localY + areaY));
                int centerBiomeId = parentBiomes[localX + 1 + (localY + 1) * parentWidth];
                BiomeGenBase centerBiome = BiomeGenBase.getBiome(centerBiomeId);

                if (centerBiomeId == BiomeGenBase.mushroomIsland.biomeID)
                {
                    int northBiome = parentBiomes[localX + 1 + localY * parentWidth];
                    int eastBiome = parentBiomes[localX + 2 + (localY + 1) * parentWidth];
                    int westBiome = parentBiomes[localX + (localY + 1) * parentWidth];
                    int southBiome = parentBiomes[localX + 1 + (localY + 2) * parentWidth];

                    if (northBiome != BiomeGenBase.ocean.biomeID && eastBiome != BiomeGenBase.ocean.biomeID && westBiome != BiomeGenBase.ocean.biomeID && southBiome != BiomeGenBase.ocean.biomeID)
                    {
                        shoreBiomes[localX + localY * areaWidth] = centerBiomeId;
                    }
                    else
                    {
                        shoreBiomes[localX + localY * areaWidth] = BiomeGenBase.mushroomIslandShore.biomeID;
                    }
                }
                else if (centerBiome != null && centerBiome.getBiomeClass() == BiomeGenJungle.class)
                {
                    int northBiome = parentBiomes[localX + 1 + localY * parentWidth];
                    int eastBiome = parentBiomes[localX + 2 + (localY + 1) * parentWidth];
                    int westBiome = parentBiomes[localX + (localY + 1) * parentWidth];
                    int southBiome = parentBiomes[localX + 1 + (localY + 2) * parentWidth];

                    if (this.isJungleCompatible(northBiome) && this.isJungleCompatible(eastBiome) && this.isJungleCompatible(westBiome) && this.isJungleCompatible(southBiome))
                    {
                        if (!isBiomeOceanic(northBiome) && !isBiomeOceanic(eastBiome) && !isBiomeOceanic(westBiome) && !isBiomeOceanic(southBiome))
                        {
                            shoreBiomes[localX + localY * areaWidth] = centerBiomeId;
                        }
                        else
                        {
                            shoreBiomes[localX + localY * areaWidth] = BiomeGenBase.beach.biomeID;
                        }
                    }
                    else
                    {
                        shoreBiomes[localX + localY * areaWidth] = BiomeGenBase.jungleEdge.biomeID;
                    }
                }
                else if (centerBiomeId != BiomeGenBase.extremeHills.biomeID && centerBiomeId != BiomeGenBase.extremeHillsPlus.biomeID && centerBiomeId != BiomeGenBase.extremeHillsEdge.biomeID)
                {
                    if (centerBiome != null && centerBiome.isSnowyBiome())
                    {
                        this.replaceIfNeighborOcean(parentBiomes, shoreBiomes, localX, localY, areaWidth, centerBiomeId, BiomeGenBase.coldBeach.biomeID);
                    }
                    else if (centerBiomeId != BiomeGenBase.mesa.biomeID && centerBiomeId != BiomeGenBase.mesaPlateau_F.biomeID)
                    {
                        if (centerBiomeId != BiomeGenBase.ocean.biomeID && centerBiomeId != BiomeGenBase.deepOcean.biomeID && centerBiomeId != BiomeGenBase.river.biomeID && centerBiomeId != BiomeGenBase.swampland.biomeID)
                        {
                            int northBiome = parentBiomes[localX + 1 + localY * parentWidth];
                            int eastBiome = parentBiomes[localX + 2 + (localY + 1) * parentWidth];
                            int westBiome = parentBiomes[localX + (localY + 1) * parentWidth];
                            int southBiome = parentBiomes[localX + 1 + (localY + 2) * parentWidth];

                            if (!isBiomeOceanic(northBiome) && !isBiomeOceanic(eastBiome) && !isBiomeOceanic(westBiome) && !isBiomeOceanic(southBiome))
                            {
                                shoreBiomes[localX + localY * areaWidth] = centerBiomeId;
                            }
                            else
                            {
                                shoreBiomes[localX + localY * areaWidth] = BiomeGenBase.beach.biomeID;
                            }
                        }
                        else
                        {
                            shoreBiomes[localX + localY * areaWidth] = centerBiomeId;
                        }
                    }
                    else
                    {
                        int northBiome = parentBiomes[localX + 1 + localY * parentWidth];
                        int eastBiome = parentBiomes[localX + 2 + (localY + 1) * parentWidth];
                        int westBiome = parentBiomes[localX + (localY + 1) * parentWidth];
                        int southBiome = parentBiomes[localX + 1 + (localY + 2) * parentWidth];

                        if (!isBiomeOceanic(northBiome) && !isBiomeOceanic(eastBiome) && !isBiomeOceanic(westBiome) && !isBiomeOceanic(southBiome))
                        {
                            if (this.isMesa(northBiome) && this.isMesa(eastBiome) && this.isMesa(westBiome) && this.isMesa(southBiome))
                            {
                                shoreBiomes[localX + localY * areaWidth] = centerBiomeId;
                            }
                            else
                            {
                                shoreBiomes[localX + localY * areaWidth] = BiomeGenBase.desert.biomeID;
                            }
                        }
                        else
                        {
                            shoreBiomes[localX + localY * areaWidth] = centerBiomeId;
                        }
                    }
                }
                else
                {
                    this.replaceIfNeighborOcean(parentBiomes, shoreBiomes, localX, localY, areaWidth, centerBiomeId, BiomeGenBase.stoneBeach.biomeID);
                }
            }
        }

        return shoreBiomes;
    }

    private void replaceIfNeighborOcean(int[] parentBiomes, int[] outputBiomes, int x, int y, int areaWidth, int biomeId, int shoreBiomeId)
    {
        if (isBiomeOceanic(biomeId))
        {
            outputBiomes[x + y * areaWidth] = biomeId;
        }
        else
        {
            int parentWidth = areaWidth + 2;
            int northBiome = parentBiomes[x + 1 + y * parentWidth];
            int eastBiome = parentBiomes[x + 2 + (y + 1) * parentWidth];
            int westBiome = parentBiomes[x + (y + 1) * parentWidth];
            int southBiome = parentBiomes[x + 1 + (y + 2) * parentWidth];

            if (!isBiomeOceanic(northBiome) && !isBiomeOceanic(eastBiome) && !isBiomeOceanic(westBiome) && !isBiomeOceanic(southBiome))
            {
                outputBiomes[x + y * areaWidth] = biomeId;
            }
            else
            {
                outputBiomes[x + y * areaWidth] = shoreBiomeId;
            }
        }
    }

    private boolean isJungleCompatible(int biomeId)
    {
        return BiomeGenBase.getBiome(biomeId) != null && BiomeGenBase.getBiome(biomeId).getBiomeClass() == BiomeGenJungle.class ? true : biomeId == BiomeGenBase.jungleEdge.biomeID || biomeId == BiomeGenBase.jungle.biomeID || biomeId == BiomeGenBase.jungleHills.biomeID || biomeId == BiomeGenBase.forest.biomeID || biomeId == BiomeGenBase.taiga.biomeID || isBiomeOceanic(biomeId);
    }

    private boolean isMesa(int biomeId)
    {
        return BiomeGenBase.getBiome(biomeId) instanceof BiomeGenMesa;
    }
}
