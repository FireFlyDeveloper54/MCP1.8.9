package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.util.MathHelper;

public class NoiseGeneratorOctaves extends NoiseGenerator
{
    private NoiseGeneratorImproved[] generatorCollection;
    private int octaves;

    public NoiseGeneratorOctaves(Random seed, int octavesIn)
    {
        this.octaves = octavesIn;
        this.generatorCollection = new NoiseGeneratorImproved[octavesIn];

        for (int octaveIndex = 0; octaveIndex < octavesIn; ++octaveIndex)
        {
            this.generatorCollection[octaveIndex] = new NoiseGeneratorImproved(seed);
        }
    }

    public double[] generateNoiseOctaves(double[] noiseArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale)
    {
        if (noiseArray == null)
        {
            noiseArray = new double[xSize * ySize * zSize];
        }
        else
        {
            for (int bufferIndex = 0; bufferIndex < noiseArray.length; ++bufferIndex)
            {
                noiseArray[bufferIndex] = 0.0D;
            }
        }

        double octaveScale = 1.0D;

        for (int octaveIndex = 0; octaveIndex < this.octaves; ++octaveIndex)
        {
            double scaledXOffset = (double)xOffset * octaveScale * xScale;
            double scaledYOffset = (double)yOffset * octaveScale * yScale;
            double scaledZOffset = (double)zOffset * octaveScale * zScale;
            long wrappedXOffset = MathHelper.floor_double_long(scaledXOffset);
            long wrappedZOffset = MathHelper.floor_double_long(scaledZOffset);
            scaledXOffset = scaledXOffset - (double)wrappedXOffset;
            scaledZOffset = scaledZOffset - (double)wrappedZOffset;
            wrappedXOffset = wrappedXOffset % 16777216L;
            wrappedZOffset = wrappedZOffset % 16777216L;
            scaledXOffset = scaledXOffset + (double)wrappedXOffset;
            scaledZOffset = scaledZOffset + (double)wrappedZOffset;
            this.generatorCollection[octaveIndex].populateNoiseArray(noiseArray, scaledXOffset, scaledYOffset, scaledZOffset, xSize, ySize, zSize, xScale * octaveScale, yScale * octaveScale, zScale * octaveScale, octaveScale);
            octaveScale /= 2.0D;
        }

        return noiseArray;
    }

    public double[] generateNoiseOctaves(double[] noiseArray, int xOffset, int zOffset, int xSize, int zSize, double xScale, double zScale, double unusedScale)
    {
        return this.generateNoiseOctaves(noiseArray, xOffset, 10, zOffset, xSize, 1, zSize, xScale, 1.0D, zScale);
    }
}
