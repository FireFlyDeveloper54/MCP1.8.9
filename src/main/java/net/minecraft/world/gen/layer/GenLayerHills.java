package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenLayerHills extends GenLayer
{
    private static final Logger logger = LogManager.getLogger();
    private GenLayer riverLayer;

    public GenLayerHills(long baseSeedIn, GenLayer parent, GenLayer riverLayerIn)
    {
        super(baseSeedIn);
        this.parent = parent;
        this.riverLayer = riverLayerIn;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] parentBiomes = this.parent.getInts(areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] riverValues = this.riverLayer.getInts(areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] outputBiomes = IntCache.getIntCache(areaWidth * areaHeight);

        for (int y = 0; y < areaHeight; ++y)
        {
            for (int x = 0; x < areaWidth; ++x)
            {
                this.initChunkSeed((long)(x + areaX), (long)(y + areaY));
                int centerBiomeId = parentBiomes[x + 1 + (y + 1) * (areaWidth + 2)];
                int riverValue = riverValues[x + 1 + (y + 1) * (areaWidth + 2)];
                boolean forceMutation = (riverValue - 2) % 29 == 0;

                if (centerBiomeId > 255)
                {
                    logger.debug("old! " + centerBiomeId);
                }

                if (centerBiomeId != 0 && riverValue >= 2 && (riverValue - 2) % 29 == 1 && centerBiomeId < 128)
                {
                    if (BiomeGenBase.getBiome(centerBiomeId + 128) != null)
                    {
                        outputBiomes[x + y * areaWidth] = centerBiomeId + 128;
                    }
                    else
                    {
                        outputBiomes[x + y * areaWidth] = centerBiomeId;
                    }
                }
                else if (this.nextInt(3) != 0 && !forceMutation)
                {
                    outputBiomes[x + y * areaWidth] = centerBiomeId;
                }
                else
                {
                    int mutatedBiomeId = centerBiomeId;

                    if (centerBiomeId == BiomeGenBase.desert.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.desertHills.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.forest.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.forestHills.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.birchForest.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.birchForestHills.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.roofedForest.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.plains.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.taiga.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.taigaHills.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.megaTaiga.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.megaTaigaHills.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.coldTaiga.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.coldTaigaHills.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.plains.biomeID)
                    {
                        if (this.nextInt(3) == 0)
                        {
                            mutatedBiomeId = BiomeGenBase.forestHills.biomeID;
                        }
                        else
                        {
                            mutatedBiomeId = BiomeGenBase.forest.biomeID;
                        }
                    }
                    else if (centerBiomeId == BiomeGenBase.icePlains.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.iceMountains.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.jungle.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.jungleHills.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.ocean.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.deepOcean.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.extremeHills.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.extremeHillsPlus.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.savanna.biomeID)
                    {
                        mutatedBiomeId = BiomeGenBase.savannaPlateau.biomeID;
                    }
                    else if (biomesEqualOrMesaPlateau(centerBiomeId, BiomeGenBase.mesaPlateau_F.biomeID))
                    {
                        mutatedBiomeId = BiomeGenBase.mesa.biomeID;
                    }
                    else if (centerBiomeId == BiomeGenBase.deepOcean.biomeID && this.nextInt(3) == 0)
                    {
                        int islandBiomeChoice = this.nextInt(2);

                        if (islandBiomeChoice == 0)
                        {
                            mutatedBiomeId = BiomeGenBase.plains.biomeID;
                        }
                        else
                        {
                            mutatedBiomeId = BiomeGenBase.forest.biomeID;
                        }
                    }

                    if (forceMutation && mutatedBiomeId != centerBiomeId)
                    {
                        if (BiomeGenBase.getBiome(mutatedBiomeId + 128) != null)
                        {
                            mutatedBiomeId += 128;
                        }
                        else
                        {
                            mutatedBiomeId = centerBiomeId;
                        }
                    }

                    if (mutatedBiomeId == centerBiomeId)
                    {
                        outputBiomes[x + y * areaWidth] = centerBiomeId;
                    }
                    else
                    {
                        int northBiomeId = parentBiomes[x + 1 + (y + 1 - 1) * (areaWidth + 2)];
                        int eastBiomeId = parentBiomes[x + 1 + 1 + (y + 1) * (areaWidth + 2)];
                        int westBiomeId = parentBiomes[x + 1 - 1 + (y + 1) * (areaWidth + 2)];
                        int southBiomeId = parentBiomes[x + 1 + (y + 1 + 1) * (areaWidth + 2)];
                        int matchingNeighborCount = 0;

                        if (biomesEqualOrMesaPlateau(northBiomeId, centerBiomeId))
                        {
                            ++matchingNeighborCount;
                        }

                        if (biomesEqualOrMesaPlateau(eastBiomeId, centerBiomeId))
                        {
                            ++matchingNeighborCount;
                        }

                        if (biomesEqualOrMesaPlateau(westBiomeId, centerBiomeId))
                        {
                            ++matchingNeighborCount;
                        }

                        if (biomesEqualOrMesaPlateau(southBiomeId, centerBiomeId))
                        {
                            ++matchingNeighborCount;
                        }

                        if (matchingNeighborCount >= 3)
                        {
                            outputBiomes[x + y * areaWidth] = mutatedBiomeId;
                        }
                        else
                        {
                            outputBiomes[x + y * areaWidth] = centerBiomeId;
                        }
                    }
                }
            }
        }

        return outputBiomes;
    }
}
