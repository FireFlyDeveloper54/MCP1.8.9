package net.minecraft.world.gen.layer;

public class GenLayerEdge extends GenLayer
{
    private final GenLayerEdge.Mode mode;

    public GenLayerEdge(long baseSeedIn, GenLayer parent, GenLayerEdge.Mode modeIn)
    {
        super(baseSeedIn);
        this.parent = parent;
        this.mode = modeIn;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        switch (this.mode)
        {
            case COOL_WARM:
            default:
                return this.getIntsCoolWarm(areaX, areaY, areaWidth, areaHeight);

            case HEAT_ICE:
                return this.getIntsHeatIce(areaX, areaY, areaWidth, areaHeight);

            case SPECIAL:
                return this.getIntsSpecial(areaX, areaY, areaWidth, areaHeight);
        }
    }

    private int[] getIntsCoolWarm(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int parentAreaX = areaX - 1;
        int parentAreaY = areaY - 1;
        int parentWidth = 1 + areaWidth + 1;
        int parentHeight = 1 + areaHeight + 1;
        int[] parentValues = this.parent.getInts(parentAreaX, parentAreaY, parentWidth, parentHeight);
        int[] outputValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int y = 0; y < areaHeight; ++y)
        {
            for (int x = 0; x < areaWidth; ++x)
            {
                this.initChunkSeed((long)(x + areaX), (long)(y + areaY));
                int centerValue = parentValues[x + 1 + (y + 1) * parentWidth];

                if (centerValue == 1)
                {
                    int northValue = parentValues[x + 1 + (y + 1 - 1) * parentWidth];
                    int eastValue = parentValues[x + 1 + 1 + (y + 1) * parentWidth];
                    int westValue = parentValues[x + 1 - 1 + (y + 1) * parentWidth];
                    int southValue = parentValues[x + 1 + (y + 1 + 1) * parentWidth];
                    boolean hasColdNeighbor = northValue == 3 || eastValue == 3 || westValue == 3 || southValue == 3;
                    boolean hasIceNeighbor = northValue == 4 || eastValue == 4 || westValue == 4 || southValue == 4;

                    if (hasColdNeighbor || hasIceNeighbor)
                    {
                        centerValue = 2;
                    }
                }

                outputValues[x + y * areaWidth] = centerValue;
            }
        }

        return outputValues;
    }

    private int[] getIntsHeatIce(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int parentAreaX = areaX - 1;
        int parentAreaY = areaY - 1;
        int parentWidth = 1 + areaWidth + 1;
        int parentHeight = 1 + areaHeight + 1;
        int[] parentValues = this.parent.getInts(parentAreaX, parentAreaY, parentWidth, parentHeight);
        int[] outputValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int y = 0; y < areaHeight; ++y)
        {
            for (int x = 0; x < areaWidth; ++x)
            {
                int centerValue = parentValues[x + 1 + (y + 1) * parentWidth];

                if (centerValue == 4)
                {
                    int northValue = parentValues[x + 1 + (y + 1 - 1) * parentWidth];
                    int eastValue = parentValues[x + 1 + 1 + (y + 1) * parentWidth];
                    int westValue = parentValues[x + 1 - 1 + (y + 1) * parentWidth];
                    int southValue = parentValues[x + 1 + (y + 1 + 1) * parentWidth];
                    boolean hasTemperateNeighbor = northValue == 2 || eastValue == 2 || westValue == 2 || southValue == 2;
                    boolean hasWarmNeighbor = northValue == 1 || eastValue == 1 || westValue == 1 || southValue == 1;

                    if (hasWarmNeighbor || hasTemperateNeighbor)
                    {
                        centerValue = 3;
                    }
                }

                outputValues[x + y * areaWidth] = centerValue;
            }
        }

        return outputValues;
    }

    private int[] getIntsSpecial(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] parentValues = this.parent.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] outputValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int y = 0; y < areaHeight; ++y)
        {
            for (int x = 0; x < areaWidth; ++x)
            {
                this.initChunkSeed((long)(x + areaX), (long)(y + areaY));
                int value = parentValues[x + y * areaWidth];

                if (value != 0 && this.nextInt(13) == 0)
                {
                    value |= 1 + this.nextInt(15) << 8 & 3840;
                }

                outputValues[x + y * areaWidth] = value;
            }
        }

        return outputValues;
    }

    public static enum Mode
    {
        COOL_WARM,
        HEAT_ICE,
        SPECIAL;
    }
}
