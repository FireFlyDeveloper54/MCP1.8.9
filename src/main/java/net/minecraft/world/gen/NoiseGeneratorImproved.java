package net.minecraft.world.gen;

import java.util.Random;

public class NoiseGeneratorImproved extends NoiseGenerator
{
    private int[] permutations;
    public double xCoord;
    public double yCoord;
    public double zCoord;
    private static final double[] GRAD_X = new double[] {1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, -1.0D, 0.0D};
    private static final double[] GRAD_Y = new double[] {1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D};
    private static final double[] GRAD_Z = new double[] {0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, -1.0D, -1.0D, 1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 1.0D, 0.0D, -1.0D};
    private static final double[] GRAD_2D_X = new double[] {1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, -1.0D, 0.0D};
    private static final double[] GRAD_2D_Z = new double[] {0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, -1.0D, -1.0D, 1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 1.0D, 0.0D, -1.0D};

    public NoiseGeneratorImproved()
    {
        this(new Random());
    }

    public NoiseGeneratorImproved(Random random)
    {
        this.permutations = new int[512];
        this.xCoord = random.nextDouble() * 256.0D;
        this.yCoord = random.nextDouble() * 256.0D;
        this.zCoord = random.nextDouble() * 256.0D;

        for (int index = 0; index < 256; this.permutations[index] = index++)
        {
            ;
        }

        for (int shuffleIndex = 0; shuffleIndex < 256; ++shuffleIndex)
        {
            int swapIndex = random.nextInt(256 - shuffleIndex) + shuffleIndex;
            int swapValue = this.permutations[shuffleIndex];
            this.permutations[shuffleIndex] = this.permutations[swapIndex];
            this.permutations[swapIndex] = swapValue;
            this.permutations[shuffleIndex + 256] = this.permutations[shuffleIndex];
        }
    }

    public final double lerp(double amount, double start, double end)
    {
        return start + amount * (end - start);
    }

    public final double grad2d(int hash, double x, double z)
    {
        int gradientIndex = hash & 15;
        return GRAD_2D_X[gradientIndex] * x + GRAD_2D_Z[gradientIndex] * z;
    }

    public final double grad(int hash, double x, double y, double z)
    {
        int gradientIndex = hash & 15;
        return GRAD_X[gradientIndex] * x + GRAD_Y[gradientIndex] * y + GRAD_Z[gradientIndex] * z;
    }

    public void populateNoiseArray(double[] noiseArray, double xOffset, double yOffset, double zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale, double noiseScale)
    {
        if (ySize == 1)
        {
            int xHash = 0;
            int xzHash = 0;
            int nextXHash = 0;
            int nextXzHash = 0;
            double xLerpAtZ0 = 0.0D;
            double xLerpAtZ1 = 0.0D;
            int outputIndex = 0;
            double inverseNoiseScale = 1.0D / noiseScale;

            for (int xIndex = 0; xIndex < xSize; ++xIndex)
            {
                double sampleX = xOffset + (double)xIndex * xScale + this.xCoord;
                int floorX = (int)sampleX;

                if (sampleX < (double)floorX)
                {
                    --floorX;
                }

                int xMask = floorX & 255;
                sampleX = sampleX - (double)floorX;
                double fadeX = sampleX * sampleX * sampleX * (sampleX * (sampleX * 6.0D - 15.0D) + 10.0D);

                for (int zIndex = 0; zIndex < zSize; ++zIndex)
                {
                    double sampleZ = zOffset + (double)zIndex * zScale + this.zCoord;
                    int floorZ = (int)sampleZ;

                    if (sampleZ < (double)floorZ)
                    {
                        --floorZ;
                    }

                    int zMask = floorZ & 255;
                    sampleZ = sampleZ - (double)floorZ;
                    double fadeZ = sampleZ * sampleZ * sampleZ * (sampleZ * (sampleZ * 6.0D - 15.0D) + 10.0D);
                    xHash = this.permutations[xMask];
                    xzHash = this.permutations[xHash] + zMask;
                    nextXHash = this.permutations[xMask + 1];
                    nextXzHash = this.permutations[nextXHash] + zMask;
                    xLerpAtZ0 = this.lerp(fadeX, this.grad2d(this.permutations[xzHash], sampleX, sampleZ), this.grad(this.permutations[nextXzHash], sampleX - 1.0D, 0.0D, sampleZ));
                    xLerpAtZ1 = this.lerp(fadeX, this.grad(this.permutations[xzHash + 1], sampleX, 0.0D, sampleZ - 1.0D), this.grad(this.permutations[nextXzHash + 1], sampleX - 1.0D, 0.0D, sampleZ - 1.0D));
                    double noiseValue = this.lerp(fadeZ, xLerpAtZ0, xLerpAtZ1);
                    noiseArray[outputIndex++] += noiseValue * inverseNoiseScale;
                }
            }
        }
        else
        {
            int outputIndex = 0;
            double inverseNoiseScale = 1.0D / noiseScale;
            int cachedYMask = -1;
            int xyHash = 0;
            int xyzHash = 0;
            int xy1zHash = 0;
            int nextXyHash = 0;
            int x1yzHash = 0;
            int x1y1zHash = 0;
            double xLerpAtY0Z0 = 0.0D;
            double xLerpAtY1Z0 = 0.0D;
            double xLerpAtY0Z1 = 0.0D;
            double xLerpAtY1Z1 = 0.0D;

            for (int xIndex = 0; xIndex < xSize; ++xIndex)
            {
                double sampleX = xOffset + (double)xIndex * xScale + this.xCoord;
                int floorX = (int)sampleX;

                if (sampleX < (double)floorX)
                {
                    --floorX;
                }

                int xMask = floorX & 255;
                sampleX = sampleX - (double)floorX;
                double fadeX = sampleX * sampleX * sampleX * (sampleX * (sampleX * 6.0D - 15.0D) + 10.0D);

                for (int zIndex = 0; zIndex < zSize; ++zIndex)
                {
                    double sampleZ = zOffset + (double)zIndex * zScale + this.zCoord;
                    int floorZ = (int)sampleZ;

                    if (sampleZ < (double)floorZ)
                    {
                        --floorZ;
                    }

                    int zMask = floorZ & 255;
                    sampleZ = sampleZ - (double)floorZ;
                    double fadeZ = sampleZ * sampleZ * sampleZ * (sampleZ * (sampleZ * 6.0D - 15.0D) + 10.0D);

                    for (int yIndex = 0; yIndex < ySize; ++yIndex)
                    {
                        double sampleY = yOffset + (double)yIndex * yScale + this.yCoord;
                        int floorY = (int)sampleY;

                        if (sampleY < (double)floorY)
                        {
                            --floorY;
                        }

                        int yMask = floorY & 255;
                        sampleY = sampleY - (double)floorY;
                        double fadeY = sampleY * sampleY * sampleY * (sampleY * (sampleY * 6.0D - 15.0D) + 10.0D);

                        if (yIndex == 0 || yMask != cachedYMask)
                        {
                            cachedYMask = yMask;
                            xyHash = this.permutations[xMask] + yMask;
                            xyzHash = this.permutations[xyHash] + zMask;
                            xy1zHash = this.permutations[xyHash + 1] + zMask;
                            nextXyHash = this.permutations[xMask + 1] + yMask;
                            x1yzHash = this.permutations[nextXyHash] + zMask;
                            x1y1zHash = this.permutations[nextXyHash + 1] + zMask;
                            xLerpAtY0Z0 = this.lerp(fadeX, this.grad(this.permutations[xyzHash], sampleX, sampleY, sampleZ), this.grad(this.permutations[x1yzHash], sampleX - 1.0D, sampleY, sampleZ));
                            xLerpAtY1Z0 = this.lerp(fadeX, this.grad(this.permutations[xy1zHash], sampleX, sampleY - 1.0D, sampleZ), this.grad(this.permutations[x1y1zHash], sampleX - 1.0D, sampleY - 1.0D, sampleZ));
                            xLerpAtY0Z1 = this.lerp(fadeX, this.grad(this.permutations[xyzHash + 1], sampleX, sampleY, sampleZ - 1.0D), this.grad(this.permutations[x1yzHash + 1], sampleX - 1.0D, sampleY, sampleZ - 1.0D));
                            xLerpAtY1Z1 = this.lerp(fadeX, this.grad(this.permutations[xy1zHash + 1], sampleX, sampleY - 1.0D, sampleZ - 1.0D), this.grad(this.permutations[x1y1zHash + 1], sampleX - 1.0D, sampleY - 1.0D, sampleZ - 1.0D));
                        }

                        double yLerpAtZ0 = this.lerp(fadeY, xLerpAtY0Z0, xLerpAtY1Z0);
                        double yLerpAtZ1 = this.lerp(fadeY, xLerpAtY0Z1, xLerpAtY1Z1);
                        double noiseValue = this.lerp(fadeZ, yLerpAtZ0, yLerpAtZ1);
                        noiseArray[outputIndex++] += noiseValue * inverseNoiseScale;
                    }
                }
            }
        }
    }
}
