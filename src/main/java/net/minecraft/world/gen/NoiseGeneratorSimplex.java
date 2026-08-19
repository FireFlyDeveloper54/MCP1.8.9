package net.minecraft.world.gen;

import java.util.Random;

public class NoiseGeneratorSimplex
{
    private static int[][] GRADIENTS = new int[][] {{1, 1, 0}, { -1, 1, 0}, {1, -1, 0}, { -1, -1, 0}, {1, 0, 1}, { -1, 0, 1}, {1, 0, -1}, { -1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
    public static final double SQRT_3 = Math.sqrt(3.0D);
    private int[] permutations;
    public double xOrigin;
    public double yOrigin;
    public double zOrigin;
    private static final double SKEW_FACTOR_2D = 0.5D * (SQRT_3 - 1.0D);
    private static final double UNSKEW_FACTOR_2D = (3.0D - SQRT_3) / 6.0D;

    public NoiseGeneratorSimplex()
    {
        this(new Random());
    }

    public NoiseGeneratorSimplex(Random random)
    {
        this.permutations = new int[512];
        this.xOrigin = random.nextDouble() * 256.0D;
        this.yOrigin = random.nextDouble() * 256.0D;
        this.zOrigin = random.nextDouble() * 256.0D;

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

    private static int fastFloor(double value)
    {
        return value > 0.0D ? (int)value : (int)value - 1;
    }

    private static double dot(int[] gradient, double x, double y)
    {
        return (double)gradient[0] * x + (double)gradient[1] * y;
    }

    public double getValue(double x, double y)
    {
        double skewOffset = (x + y) * SKEW_FACTOR_2D;
        int cellX = fastFloor(x + skewOffset);
        int cellY = fastFloor(y + skewOffset);
        double unskewOffset = (double)(cellX + cellY) * UNSKEW_FACTOR_2D;
        double cellOriginX = (double)cellX - unskewOffset;
        double cellOriginY = (double)cellY - unskewOffset;
        double x0 = x - cellOriginX;
        double y0 = y - cellOriginY;
        int simplexOffsetX;
        int simplexOffsetY;

        if (x0 > y0)
        {
            simplexOffsetX = 1;
            simplexOffsetY = 0;
        }
        else
        {
            simplexOffsetX = 0;
            simplexOffsetY = 1;
        }

        double x1 = x0 - (double)simplexOffsetX + UNSKEW_FACTOR_2D;
        double y1 = y0 - (double)simplexOffsetY + UNSKEW_FACTOR_2D;
        double x2 = x0 - 1.0D + 2.0D * UNSKEW_FACTOR_2D;
        double y2 = y0 - 1.0D + 2.0D * UNSKEW_FACTOR_2D;
        int xMask = cellX & 255;
        int yMask = cellY & 255;
        int gradientIndex0 = this.permutations[xMask + this.permutations[yMask]] % 12;
        int gradientIndex1 = this.permutations[xMask + simplexOffsetX + this.permutations[yMask + simplexOffsetY]] % 12;
        int gradientIndex2 = this.permutations[xMask + 1 + this.permutations[yMask + 1]] % 12;
        double falloff0 = 0.5D - x0 * x0 - y0 * y0;
        double contribution0;

        if (falloff0 < 0.0D)
        {
            contribution0 = 0.0D;
        }
        else
        {
            falloff0 = falloff0 * falloff0;
            contribution0 = falloff0 * falloff0 * dot(GRADIENTS[gradientIndex0], x0, y0);
        }

        double falloff1 = 0.5D - x1 * x1 - y1 * y1;
        double contribution1;

        if (falloff1 < 0.0D)
        {
            contribution1 = 0.0D;
        }
        else
        {
            falloff1 = falloff1 * falloff1;
            contribution1 = falloff1 * falloff1 * dot(GRADIENTS[gradientIndex1], x1, y1);
        }

        double falloff2 = 0.5D - x2 * x2 - y2 * y2;
        double contribution2;

        if (falloff2 < 0.0D)
        {
            contribution2 = 0.0D;
        }
        else
        {
            falloff2 = falloff2 * falloff2;
            contribution2 = falloff2 * falloff2 * dot(GRADIENTS[gradientIndex2], x2, y2);
        }

        return 70.0D * (contribution0 + contribution1 + contribution2);
    }

    public void add(double[] noiseArray, double xOffset, double yOffset, int xSize, int ySize, double xScale, double yScale, double noiseScale)
    {
        int outputIndex = 0;

        for (int yIndex = 0; yIndex < ySize; ++yIndex)
        {
            double sampleY = (yOffset + (double)yIndex) * yScale + this.yOrigin;

            for (int xIndex = 0; xIndex < xSize; ++xIndex)
            {
                double sampleX = (xOffset + (double)xIndex) * xScale + this.xOrigin;
                double skewOffset = (sampleX + sampleY) * SKEW_FACTOR_2D;
                int cellX = fastFloor(sampleX + skewOffset);
                int cellY = fastFloor(sampleY + skewOffset);
                double unskewOffset = (double)(cellX + cellY) * UNSKEW_FACTOR_2D;
                double cellOriginX = (double)cellX - unskewOffset;
                double cellOriginY = (double)cellY - unskewOffset;
                double x0 = sampleX - cellOriginX;
                double y0 = sampleY - cellOriginY;
                int simplexOffsetX;
                int simplexOffsetY;

                if (x0 > y0)
                {
                    simplexOffsetX = 1;
                    simplexOffsetY = 0;
                }
                else
                {
                    simplexOffsetX = 0;
                    simplexOffsetY = 1;
                }

                double x1 = x0 - (double)simplexOffsetX + UNSKEW_FACTOR_2D;
                double y1 = y0 - (double)simplexOffsetY + UNSKEW_FACTOR_2D;
                double x2 = x0 - 1.0D + 2.0D * UNSKEW_FACTOR_2D;
                double y2 = y0 - 1.0D + 2.0D * UNSKEW_FACTOR_2D;
                int xMask = cellX & 255;
                int yMask = cellY & 255;
                int gradientIndex0 = this.permutations[xMask + this.permutations[yMask]] % 12;
                int gradientIndex1 = this.permutations[xMask + simplexOffsetX + this.permutations[yMask + simplexOffsetY]] % 12;
                int gradientIndex2 = this.permutations[xMask + 1 + this.permutations[yMask + 1]] % 12;
                double falloff0 = 0.5D - x0 * x0 - y0 * y0;
                double contribution0;

                if (falloff0 < 0.0D)
                {
                    contribution0 = 0.0D;
                }
                else
                {
                    falloff0 = falloff0 * falloff0;
                    contribution0 = falloff0 * falloff0 * dot(GRADIENTS[gradientIndex0], x0, y0);
                }

                double falloff1 = 0.5D - x1 * x1 - y1 * y1;
                double contribution1;

                if (falloff1 < 0.0D)
                {
                    contribution1 = 0.0D;
                }
                else
                {
                    falloff1 = falloff1 * falloff1;
                    contribution1 = falloff1 * falloff1 * dot(GRADIENTS[gradientIndex1], x1, y1);
                }

                double falloff2 = 0.5D - x2 * x2 - y2 * y2;
                double contribution2;

                if (falloff2 < 0.0D)
                {
                    contribution2 = 0.0D;
                }
                else
                {
                    falloff2 = falloff2 * falloff2;
                    contribution2 = falloff2 * falloff2 * dot(GRADIENTS[gradientIndex2], x2, y2);
                }

                noiseArray[outputIndex++] += 70.0D * (contribution0 + contribution1 + contribution2) * noiseScale;
            }
        }
    }
}
