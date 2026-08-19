package net.minecraft.world.gen.layer;

public class GenLayerZoom extends GenLayer
{
    public GenLayerZoom(long baseSeedIn, GenLayer parent)
    {
        super(baseSeedIn);
        super.parent = parent;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int parentAreaX = areaX >> 1;
        int parentAreaY = areaY >> 1;
        int parentWidth = (areaWidth >> 1) + 2;
        int parentHeight = (areaHeight >> 1) + 2;
        int[] parentValues = this.parent.getInts(parentAreaX, parentAreaY, parentWidth, parentHeight);
        int zoomedWidth = (parentWidth - 1) << 1;
        int zoomedHeight = (parentHeight - 1) << 1;
        int[] zoomedValues = IntCache.getIntCache(zoomedWidth * zoomedHeight);

        for (int cellY = 0; cellY < parentHeight - 1; ++cellY)
        {
            int outputIndex = (cellY << 1) * zoomedWidth;
            int cellX = 0;
            int topLeftValue = parentValues[cellX + cellY * parentWidth];

            for (int bottomLeftValue = parentValues[cellX + (cellY + 1) * parentWidth]; cellX < parentWidth - 1; ++cellX)
            {
                this.initChunkSeed((long)((cellX + parentAreaX) << 1), (long)((cellY + parentAreaY) << 1));
                int topRightValue = parentValues[cellX + 1 + cellY * parentWidth];
                int bottomRightValue = parentValues[cellX + 1 + (cellY + 1) * parentWidth];
                zoomedValues[outputIndex] = topLeftValue;
                zoomedValues[outputIndex++ + zoomedWidth] = this.selectRandom2(topLeftValue, bottomLeftValue);
                zoomedValues[outputIndex] = this.selectRandom2(topLeftValue, topRightValue);
                zoomedValues[outputIndex++ + zoomedWidth] = this.selectModeOrRandom(topLeftValue, topRightValue, bottomLeftValue, bottomRightValue);
                topLeftValue = topRightValue;
                bottomLeftValue = bottomRightValue;
            }
        }

        int[] croppedValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int outputY = 0; outputY < areaHeight; ++outputY)
        {
            System.arraycopy(zoomedValues, (outputY + (areaY & 1)) * zoomedWidth + (areaX & 1), croppedValues, outputY * areaWidth, areaWidth);
        }

        return croppedValues;
    }

    public static GenLayer magnify(long seed, GenLayer layer, int times)
    {
        GenLayer genLayer = layer;

        for (int zoomIndex = 0; zoomIndex < times; ++zoomIndex)
        {
            genLayer = new GenLayerZoom(seed + (long)zoomIndex, genLayer);
        }

        return genLayer;
    }

    protected int selectRandom2(int valueA, int valueB)
    {
        int selectedIndex = this.nextInt(2);
        return selectedIndex == 0 ? valueA : valueB;
    }
}
