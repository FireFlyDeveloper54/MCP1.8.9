package net.minecraft.util;

import java.util.Random;
import java.util.UUID;
import net.optifine.util.MathUtils;
import optimization.betterfps.BetterFps;
import optimization.betterfps.HybridMath;

public class MathHelper
{
    public static final float SQRT_2 = sqrt_float(2.0F);
    public static final float PI = MathUtils.roundToFloat(Math.PI);
    public static final float TWO_PI = MathUtils.roundToFloat((Math.PI * 2D));
    public static final float HALF_PI = MathUtils.roundToFloat((Math.PI / 2D));
    public static final float DEG_TO_RAD = MathUtils.roundToFloat(0.017453292519943295D);
    public static boolean fastMath = false;
    private static final int[] multiplyDeBruijnBitPosition;
    private static final double FRAC_BIAS;
    private static final double[] ASINE_TAB;
    private static final double[] COS_TAB;

    public static float sin(float value)
    {
        if (BetterFps.isHybridFastPath())
        {
            return HybridMath.sin(value);
        }

        return BetterFps.sin(value);
    }

    public static float cos(float value)
    {
        if (BetterFps.isHybridFastPath())
        {
            return HybridMath.cos(value);
        }

        return BetterFps.cos(value);
    }

    /**
     * Fills {@code out[0]=sin(value)}, {@code out[1]=cos(value)}.
     * Prefer this over separate sin/cos when both are needed for the same angle.
     */
    public static void sinCos(float value, float[] out)
    {
        if (BetterFps.isHybridFastPath())
        {
            HybridMath.sinCos(value, out);
            return;
        }

        BetterFps.sinCos(value, out);
    }

    /** Degree-domain sin (skips deg→rad on the hybrid path). */
    public static float sinDeg(float degrees)
    {
        if (BetterFps.isHybridFastPath())
        {
            return HybridMath.sinDeg(degrees);
        }

        return BetterFps.sinDeg(degrees);
    }

    public static float cosDeg(float degrees)
    {
        if (BetterFps.isHybridFastPath())
        {
            return HybridMath.cosDeg(degrees);
        }

        return BetterFps.cosDeg(degrees);
    }

    /** Fills {@code out[0]=sin(degrees)}, {@code out[1]=cos(degrees)}. */
    public static void sinCosDeg(float degrees, float[] out)
    {
        if (BetterFps.isHybridFastPath())
        {
            HybridMath.sinCosDeg(degrees, out);
            return;
        }

        BetterFps.sinCosDeg(degrees, out);
    }

    public static float sqrt_float(float value)
    {
        if (value <= 0.0F) return 0.0F;
        if (fastMath)
        {
            return value * (float)fastInvSqrt((double)value);
        }
        return (float)Math.sqrt((double)value);
    }

    public static float sqrt_double(double value)
    {
        if (value <= 0.0D) return 0.0F;
        if (fastMath)
        {
            return (float)(value * fastInvSqrt(value));
        }
        return (float)Math.sqrt(value);
    }

    public static int floor_float(float value)
    {
        int truncatedValue = (int)value;
        return value < (float)truncatedValue ? truncatedValue - 1 : truncatedValue;
    }

    public static int truncateDoubleToInt(double value)
    {
        return (int)(value + 1024.0D) - 1024;
    }

    public static int floor_double(double value)
    {
        int truncatedValue = (int)value;
        return value < (double)truncatedValue ? truncatedValue - 1 : truncatedValue;
    }

    public static long floor_double_long(double value)
    {
        long truncatedValue = (long)value;
        return value < (double)truncatedValue ? truncatedValue - 1L : truncatedValue;
    }

    public static int absFloor(double value)
    {
        return (int)(value >= 0.0D ? value : -value + 1.0D);
    }

    public static float abs(float value)
    {
        return value >= 0.0F ? value : -value;
    }

    public static int abs_int(int value)
    {
        return value >= 0 ? value : -value;
    }

    public static int ceiling_float_int(float value)
    {
        int truncatedValue = (int)value;
        return value > (float)truncatedValue ? truncatedValue + 1 : truncatedValue;
    }

    public static int ceiling_double_int(double value)
    {
        int truncatedValue = (int)value;
        return value > (double)truncatedValue ? truncatedValue + 1 : truncatedValue;
    }

    public static int clamp_int(int num, int min, int max)
    {
        return num < min ? min : (num > max ? max : num);
    }

    public static float clamp_float(float num, float min, float max)
    {
        return num < min ? min : (num > max ? max : num);
    }

    public static double clamp_double(double num, double min, double max)
    {
        return num < min ? min : (num > max ? max : num);
    }

    public static double denormalizeClamp(double lowerBnd, double upperBnd, double slide)
    {
        return slide < 0.0D ? lowerBnd : (slide > 1.0D ? upperBnd : lowerBnd + (upperBnd - lowerBnd) * slide);
    }

    public static double abs_max(double first, double second)
    {
        if (first < 0.0D)
        {
            first = -first;
        }

        if (second < 0.0D)
        {
            second = -second;
        }

        return first > second ? first : second;
    }

    public static int bucketInt(int value, int bucketSize)
    {
        return value < 0 ? -((-value - 1) / bucketSize) - 1 : value / bucketSize;
    }

    public static int getRandomIntegerInRange(Random random, int min, int max)
    {
        return min >= max ? min : random.nextInt(max - min + 1) + min;
    }

    public static float randomFloatClamp(Random random, float min, float max)
    {
        return min >= max ? min : random.nextFloat() * (max - min) + min;
    }

    public static double getRandomDoubleInRange(Random random, double min, double max)
    {
        return min >= max ? min : random.nextDouble() * (max - min) + min;
    }

    public static double average(long[] values)
    {
        long sum = 0L;

        for (long value : values)
        {
            sum += value;
        }

        return (double)sum / (double)values.length;
    }

    public static boolean epsilonEquals(float first, float second)
    {
        return abs(second - first) < 1.0E-5F;
    }

    public static int normalizeAngle(int angle, int modulo)
    {
        return (angle % modulo + modulo) % modulo;
    }

    public static float wrapAngleTo180_float(float value)
    {
        value = value % 360.0F;

        if (value >= 180.0F)
        {
            value -= 360.0F;
        }

        if (value < -180.0F)
        {
            value += 360.0F;
        }

        return value;
    }

    public static double wrapAngleTo180_double(double value)
    {
        value = value % 360.0D;

        if (value >= 180.0D)
        {
            value -= 360.0D;
        }

        if (value < -180.0D)
        {
            value += 360.0D;
        }

        return value;
    }

    public static int parseIntWithDefault(String valueString, int defaultValue)
    {
        try
        {
            return Integer.parseInt(valueString);
        }
        catch (Throwable caughtThrowable)
        {
            return defaultValue;
        }
    }

    public static int parseIntWithDefaultAndMax(String valueString, int defaultValue, int min)
    {
        return Math.max(min, parseIntWithDefault(valueString, defaultValue));
    }

    public static double parseDoubleWithDefault(String valueString, double defaultValue)
    {
        try
        {
            return Double.parseDouble(valueString);
        }
        catch (Throwable caughtThrowable)
        {
            return defaultValue;
        }
    }

    public static double parseDoubleWithDefaultAndMax(String valueString, double defaultValue, double min)
    {
        return Math.max(min, parseDoubleWithDefault(valueString, defaultValue));
    }

    public static int roundUpToPowerOfTwo(int value)
    {
        int roundedValue = value - 1;
        roundedValue = roundedValue | roundedValue >> 1;
        roundedValue = roundedValue | roundedValue >> 2;
        roundedValue = roundedValue | roundedValue >> 4;
        roundedValue = roundedValue | roundedValue >> 8;
        roundedValue = roundedValue | roundedValue >> 16;
        return roundedValue + 1;
    }

    private static boolean isPowerOfTwo(int value)
    {
        return value != 0 && (value & value - 1) == 0;
    }

    private static int calculateLogBaseTwoDeBruijn(int value)
    {
        value = isPowerOfTwo(value) ? value : roundUpToPowerOfTwo(value);
        return multiplyDeBruijnBitPosition[(int)((long)value * 125613361L >> 27) & 31];
    }

    public static int calculateLogBaseTwo(int value)
    {
        return calculateLogBaseTwoDeBruijn(value) - (isPowerOfTwo(value) ? 0 : 1);
    }

    public static int roundUp(int value, int interval)
    {
        if (interval == 0)
        {
            return 0;
        }
        else if (value == 0)
        {
            return interval;
        }
        else
        {
            if (value < 0)
            {
                interval *= -1;
            }

            int remainder = value % interval;
            return remainder == 0 ? value : value + interval - remainder;
        }
    }

    public static int rgb(float red, float green, float blue)
    {
        return rgb(floor_float(red * 255.0F), floor_float(green * 255.0F), floor_float(blue * 255.0F));
    }

    public static int rgb(int red, int green, int blue)
    {
        int packedColor = (red << 8) + green;
        packedColor = (packedColor << 8) + blue;
        return packedColor;
    }

    public static int multiplyColor(int firstColor, int secondColor)
    {
        int firstRed = (firstColor & 16711680) >> 16;
        int secondRed = (secondColor & 16711680) >> 16;
        int firstGreen = (firstColor & 65280) >> 8;
        int secondGreen = (secondColor & 65280) >> 8;
        int firstBlue = (firstColor & 255) >> 0;
        int secondBlue = (secondColor & 255) >> 0;
        int multipliedRed = (int)((float)firstRed * (float)secondRed / 255.0F);
        int multipliedGreen = (int)((float)firstGreen * (float)secondGreen / 255.0F);
        int multipliedBlue = (int)((float)firstBlue * (float)secondBlue / 255.0F);
        return firstColor & -16777216 | multipliedRed << 16 | multipliedGreen << 8 | multipliedBlue;
    }

    public static double frac(double value)
    {
        return value - Math.floor(value);
    }

    public static long getPositionRandom(Vec3i pos)
    {
        return getCoordinateRandom(pos.getX(), pos.getY(), pos.getZ());
    }

    public static long getCoordinateRandom(int x, int y, int z)
    {
        long seed = (long)(x * 3129871) ^ (long)z * 116129781L ^ (long)y;
        seed = seed * seed * 42317861L + seed * 11L;
        return seed;
    }

    public static UUID getRandomUuid(Random rand)
    {
        long mostSigBits = rand.nextLong() & -61441L | 16384L;
        long leastSigBits = rand.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
        return new UUID(mostSigBits, leastSigBits);
    }

    public static double getLerpProgress(double value, double start, double end)
    {
        return (value - start) / (end - start);
    }

    public static double atan2(double y, double x)
    {
        double distanceSq = x * x + y * y;

        if (Double.isNaN(distanceSq))
        {
            return Double.NaN;
        }
        else
        {
            boolean negateY = y < 0.0D;

            if (negateY)
            {
                y = -y;
            }

            boolean negateX = x < 0.0D;

            if (negateX)
            {
                x = -x;
            }

            boolean swapAxes = y > x;

            if (swapAxes)
            {
                double originalX = x;
                x = y;
                y = originalX;
            }

            double invLength = fastInvSqrt(distanceSq);
            x = x * invLength;
            y = y * invLength;
            double biasedY = FRAC_BIAS + y;
            int tableIndex = (int)Double.doubleToRawLongBits(biasedY);
            double asinValue = ASINE_TAB[tableIndex];
            double cosValue = COS_TAB[tableIndex];
            double roundedY = biasedY - FRAC_BIAS;
            double asinError = y * cosValue - x * roundedY;
            double asinCorrection = (6.0D + asinError * asinError) * asinError * 0.16666666666666666D;
            double resultAngle = asinValue + asinCorrection;

            if (swapAxes)
            {
                resultAngle = (Math.PI / 2D) - resultAngle;
            }

            if (negateX)
            {
                resultAngle = Math.PI - resultAngle;
            }

            if (negateY)
            {
                resultAngle = -resultAngle;
            }

            return resultAngle;
        }
    }

    public static double fastInvSqrt(double value)
    {
        double halfValue = 0.5D * value;
        long bits = Double.doubleToRawLongBits(value);
        bits = 6910469410427058090L - (bits >> 1);
        value = Double.longBitsToDouble(bits);
        value = value * (1.5D - halfValue * value * value);
        return value;
    }

    public static double fastSqrt_double(double value)
    {
        if (value <= 0.0D) return 0.0D;
        if (fastMath) return value * fastInvSqrt(value);
        return Math.sqrt(value);
    }

    public static float fastSqrt_float(float value)
    {
        if (value <= 0.0F) return 0.0F;
        if (fastMath) return value * (float)fastInvSqrt((double)value);
        return (float)Math.sqrt((double)value);
    }

    public static double length_double(double x, double y, double z)
    {
        double lenSq = x * x + y * y + z * z;
        if (lenSq <= 0.0D) return 0.0D;
        if (fastMath) return lenSq * fastInvSqrt(lenSq);
        return Math.sqrt(lenSq);
    }

    public static double length_double(double x, double z)
    {
        double lenSq = x * x + z * z;
        if (lenSq <= 0.0D) return 0.0D;
        if (fastMath) return lenSq * fastInvSqrt(lenSq);
        return Math.sqrt(lenSq);
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return length_double(dx, dy, dz);
    }

    public static float atan2(float y, float x)
    {
        return optimization.FastTrig.atan2((double)y, (double)x);
    }

    public static double atan2_double(double y, double x)
    {
        return (double)optimization.FastTrig.atan2(y, x);
    }

    public static int hsvToRGB(float hue, float saturation, float value)
    {
        int hueSection = (int)(hue * 6.0F) % 6;
        float fractionalHue = hue * 6.0F - (float)hueSection;
        float minValue = value * (1.0F - saturation);
        float descendingValue = value * (1.0F - fractionalHue * saturation);
        float ascendingValue = value * (1.0F - (1.0F - fractionalHue) * saturation);
        float outputRed;
        float outputGreen;
        float outputBlue;

        switch (hueSection)
        {
            case 0:
                outputRed = value;
                outputGreen = ascendingValue;
                outputBlue = minValue;
                break;

            case 1:
                outputRed = descendingValue;
                outputGreen = value;
                outputBlue = minValue;
                break;

            case 2:
                outputRed = minValue;
                outputGreen = value;
                outputBlue = ascendingValue;
                break;

            case 3:
                outputRed = minValue;
                outputGreen = descendingValue;
                outputBlue = value;
                break;

            case 4:
                outputRed = ascendingValue;
                outputGreen = minValue;
                outputBlue = value;
                break;

            case 5:
                outputRed = value;
                outputGreen = minValue;
                outputBlue = descendingValue;
                break;

            default:
                throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
        }

        int red = clamp_int((int)(outputRed * 255.0F), 0, 255);
        int green = clamp_int((int)(outputGreen * 255.0F), 0, 255);
        int blue = clamp_int((int)(outputBlue * 255.0F), 0, 255);
        return red << 16 | green << 8 | blue;
    }

    static
    {
        {
        }

        multiplyDeBruijnBitPosition = new int[] {0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
        FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
        ASINE_TAB = new double[257];
        COS_TAB = new double[257];

        for (int asinIndex = 0; asinIndex < 257; ++asinIndex)
        {
            double asinInput = (double)asinIndex / 256.0D;
            double asinValue = Math.asin(asinInput);
            COS_TAB[asinIndex] = Math.cos(asinValue);
            ASINE_TAB[asinIndex] = asinValue;
        }
    }
}
