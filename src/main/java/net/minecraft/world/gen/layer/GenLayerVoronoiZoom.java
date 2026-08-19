package net.minecraft.world.gen.layer;

public class GenLayerVoronoiZoom extends GenLayer
{
    public GenLayerVoronoiZoom(long baseSeedIn, GenLayer parent)
    {
        super(baseSeedIn);
        super.parent = parent;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        areaX = areaX - 2;
        areaY = areaY - 2;
        int parentAreaX = areaX >> 2;
        int parentAreaY = areaY >> 2;
        int parentWidth = (areaWidth >> 2) + 2;
        int parentHeight = (areaHeight >> 2) + 2;
        int[] parentValues = this.parent.getInts(parentAreaX, parentAreaY, parentWidth, parentHeight);
        int zoomedWidth = (parentWidth - 1) << 2;
        int zoomedHeight = (parentHeight - 1) << 2;
        int[] zoomedValues = IntCache.getIntCache(zoomedWidth * zoomedHeight);

        for (int cellY = 0; cellY < parentHeight - 1; ++cellY)
        {
            int cellX = 0;
            int topLeftValue = parentValues[cellX + cellY * parentWidth];

            for (int bottomLeftValue = parentValues[cellX + (cellY + 1) * parentWidth]; cellX < parentWidth - 1; ++cellX)
            {
                double jitterScale = 3.6D;
                this.initChunkSeed((long)((cellX + parentAreaX) << 2), (long)((cellY + parentAreaY) << 2));
                double topLeftOffsetX = ((double)this.nextInt(1024) / 1024.0D - 0.5D) * jitterScale;
                double topLeftOffsetY = ((double)this.nextInt(1024) / 1024.0D - 0.5D) * jitterScale;
                this.initChunkSeed((long)((cellX + parentAreaX + 1) << 2), (long)((cellY + parentAreaY) << 2));
                double topRightOffsetX = ((double)this.nextInt(1024) / 1024.0D - 0.5D) * jitterScale + 4.0D;
                double topRightOffsetY = ((double)this.nextInt(1024) / 1024.0D - 0.5D) * jitterScale;
                this.initChunkSeed((long)((cellX + parentAreaX) << 2), (long)((cellY + parentAreaY + 1) << 2));
                double bottomLeftOffsetX = ((double)this.nextInt(1024) / 1024.0D - 0.5D) * jitterScale;
                double bottomLeftOffsetY = ((double)this.nextInt(1024) / 1024.0D - 0.5D) * jitterScale + 4.0D;
                this.initChunkSeed((long)((cellX + parentAreaX + 1) << 2), (long)((cellY + parentAreaY + 1) << 2));
                double bottomRightOffsetX = ((double)this.nextInt(1024) / 1024.0D - 0.5D) * jitterScale + 4.0D;
                double bottomRightOffsetY = ((double)this.nextInt(1024) / 1024.0D - 0.5D) * jitterScale + 4.0D;
                int topRightValue = parentValues[cellX + 1 + cellY * parentWidth] & 255;
                int bottomRightValue = parentValues[cellX + 1 + (cellY + 1) * parentWidth] & 255;

                for (int localY = 0; localY < 4; ++localY)
                {
                    int outputIndex = ((cellY << 2) + localY) * zoomedWidth + (cellX << 2);

                    for (int localX = 0; localX < 4; ++localX)
                    {
                        double topLeftDistance = ((double)localY - topLeftOffsetY) * ((double)localY - topLeftOffsetY) + ((double)localX - topLeftOffsetX) * ((double)localX - topLeftOffsetX);
                        double topRightDistance = ((double)localY - topRightOffsetY) * ((double)localY - topRightOffsetY) + ((double)localX - topRightOffsetX) * ((double)localX - topRightOffsetX);
                        double bottomLeftDistance = ((double)localY - bottomLeftOffsetY) * ((double)localY - bottomLeftOffsetY) + ((double)localX - bottomLeftOffsetX) * ((double)localX - bottomLeftOffsetX);
                        double bottomRightDistance = ((double)localY - bottomRightOffsetY) * ((double)localY - bottomRightOffsetY) + ((double)localX - bottomRightOffsetX) * ((double)localX - bottomRightOffsetX);

                        if (topLeftDistance < topRightDistance && topLeftDistance < bottomLeftDistance && topLeftDistance < bottomRightDistance)
                        {
                            zoomedValues[outputIndex++] = topLeftValue;
                        }
                        else if (topRightDistance < topLeftDistance && topRightDistance < bottomLeftDistance && topRightDistance < bottomRightDistance)
                        {
                            zoomedValues[outputIndex++] = topRightValue;
                        }
                        else if (bottomLeftDistance < topLeftDistance && bottomLeftDistance < topRightDistance && bottomLeftDistance < bottomRightDistance)
                        {
                            zoomedValues[outputIndex++] = bottomLeftValue;
                        }
                        else
                        {
                            zoomedValues[outputIndex++] = bottomRightValue;
                        }
                    }
                }

                topLeftValue = topRightValue;
                bottomLeftValue = bottomRightValue;
            }
        }

        int[] croppedValues = IntCache.getIntCache(areaWidth * areaHeight);

        for (int outputY = 0; outputY < areaHeight; ++outputY)
        {
            System.arraycopy(zoomedValues, (outputY + (areaY & 3)) * zoomedWidth + (areaX & 3), croppedValues, outputY * areaWidth, areaWidth);
        }

        return croppedValues;
    }
}
