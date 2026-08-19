package net.minecraft.world.gen.layer;

public class GenLayerAddSnow extends GenLayer
{
    public GenLayerAddSnow(long baseSeedIn, GenLayer parent)
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
        int[] parentInts = this.parent.getInts(parentAreaX, parentAreaY, parentWidth, parentHeight);
        int[] snowInts = IntCache.getIntCache(areaWidth * areaHeight);

        for (int row = 0; row < areaHeight; ++row)
        {
            for (int column = 0; column < areaWidth; ++column)
            {
                int parentBiome = parentInts[column + 1 + (row + 1) * parentWidth];
                this.initChunkSeed((long)(column + areaX), (long)(row + areaY));

                if (parentBiome == 0)
                {
                    snowInts[column + row * areaWidth] = 0;
                }
                else
                {
                    int snowBiome = this.nextInt(6);

                    if (snowBiome == 0)
                    {
                        snowBiome = 4;
                    }
                    else if (snowBiome <= 1)
                    {
                        snowBiome = 3;
                    }
                    else
                    {
                        snowBiome = 1;
                    }

                    snowInts[column + row * areaWidth] = snowBiome;
                }
            }
        }

        return snowInts;
    }
}
