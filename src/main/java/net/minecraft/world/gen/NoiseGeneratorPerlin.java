package net.minecraft.world.gen;

import java.util.Random;

public class NoiseGeneratorPerlin extends NoiseGenerator
{
    private NoiseGeneratorSimplex[] noiseLevels;
    private int levels;

    public NoiseGeneratorPerlin(Random random, int levels)
    {
        this.levels = levels;
        this.noiseLevels = new NoiseGeneratorSimplex[levels];

        for (int levelIndex = 0; levelIndex < levels; ++levelIndex)
        {
            this.noiseLevels[levelIndex] = new NoiseGeneratorSimplex(random);
        }
    }

    public double getValue(double x, double y)
    {
        double noiseValue = 0.0D;
        double octaveScale = 1.0D;

        for (int levelIndex = 0; levelIndex < this.levels; ++levelIndex)
        {
            noiseValue += this.noiseLevels[levelIndex].getValue(x * octaveScale, y * octaveScale) / octaveScale;
            octaveScale /= 2.0D;
        }

        return noiseValue;
    }

    public double[] getRegion(double[] buffer, double x, double y, int width, int height, double xScale, double yScale, double persistence)
    {
        return this.getRegion(buffer, x, y, width, height, xScale, yScale, persistence, 0.5D);
    }

    public double[] getRegion(double[] buffer, double x, double y, int width, int height, double xScale, double yScale, double amplitudeScale, double persistence)
    {
        if (buffer != null && buffer.length >= width * height)
        {
            for (int bufferIndex = 0; bufferIndex < buffer.length; ++bufferIndex)
            {
                buffer[bufferIndex] = 0.0D;
            }
        }
        else
        {
            buffer = new double[width * height];
        }

        double persistenceMultiplier = 1.0D;
        double frequencyMultiplier = 1.0D;

        for (int levelIndex = 0; levelIndex < this.levels; ++levelIndex)
        {
            this.noiseLevels[levelIndex].add(buffer, x, y, width, height, xScale * frequencyMultiplier * persistenceMultiplier, yScale * frequencyMultiplier * persistenceMultiplier, 0.55D / persistenceMultiplier);
            frequencyMultiplier *= amplitudeScale;
            persistenceMultiplier *= persistence;
        }

        return buffer;
    }
}
