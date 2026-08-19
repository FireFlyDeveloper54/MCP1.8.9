package net.minecraft.world.gen.layer;

public class GenLayerAddIsland extends GenLayer
{
    public GenLayerAddIsland(long baseSeedIn, GenLayer parent)
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
        int[] islandValues = IntCache.getIntCache(areaWidth * areaHeight);

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

                if (centerBiome != 0 || northwestBiome == 0 && northeastBiome == 0 && southwestBiome == 0 && southeastBiome == 0)
                {
                    if (centerBiome > 0 && (northwestBiome == 0 || northeastBiome == 0 || southwestBiome == 0 || southeastBiome == 0))
                    {
                        if (this.nextInt(5) == 0)
                        {
                            if (centerBiome == 4)
                            {
                                islandValues[localX + localY * areaWidth] = 4;
                            }
                            else
                            {
                                islandValues[localX + localY * areaWidth] = 0;
                            }
                        }
                        else
                        {
                            islandValues[localX + localY * areaWidth] = centerBiome;
                        }
                    }
                    else
                    {
                        islandValues[localX + localY * areaWidth] = centerBiome;
                    }
                }
                else
                {
                    int candidateCount = 1;
                    int selectedNeighborBiome = 1;

                    if (northwestBiome != 0 && this.nextInt(candidateCount++) == 0)
                    {
                        selectedNeighborBiome = northwestBiome;
                    }

                    if (northeastBiome != 0 && this.nextInt(candidateCount++) == 0)
                    {
                        selectedNeighborBiome = northeastBiome;
                    }

                    if (southwestBiome != 0 && this.nextInt(candidateCount++) == 0)
                    {
                        selectedNeighborBiome = southwestBiome;
                    }

                    if (southeastBiome != 0 && this.nextInt(candidateCount++) == 0)
                    {
                        selectedNeighborBiome = southeastBiome;
                    }

                    if (this.nextInt(3) == 0)
                    {
                        islandValues[localX + localY * areaWidth] = selectedNeighborBiome;
                    }
                    else if (selectedNeighborBiome == 4)
                    {
                        islandValues[localX + localY * areaWidth] = 4;
                    }
                    else
                    {
                        islandValues[localX + localY * areaWidth] = 0;
                    }
                }
            }
        }

        return islandValues;
    }
}
