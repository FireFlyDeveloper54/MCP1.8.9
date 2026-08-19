package net.minecraft.realms;

import java.util.Random;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.StringUtils;

public class RealmsMth
{
    public static float sin(float value)
    {
        return MathHelper.sin(value);
    }

    public static double nextDouble(Random random, double min, double max)
    {
        return MathHelper.getRandomDoubleInRange(random, min, max);
    }

    public static int ceil(float value)
    {
        return MathHelper.ceiling_float_int(value);
    }

    public static int floor(double value)
    {
        return MathHelper.floor_double(value);
    }

    public static int intFloorDiv(int value, int divisor)
    {
        return MathHelper.bucketInt(value, divisor);
    }

    public static float abs(float value)
    {
        return MathHelper.abs(value);
    }

    public static int clamp(int value, int min, int max)
    {
        return MathHelper.clamp_int(value, min, max);
    }

    public static double clampedLerp(double lowerBnd, double upperBnd, double slide)
    {
        return MathHelper.denormalizeClamp(lowerBnd, upperBnd, slide);
    }

    public static int ceil(double value)
    {
        return MathHelper.ceiling_double_int(value);
    }

    public static boolean isEmpty(String value)
    {
        return StringUtils.isEmpty(value);
    }

    public static long lfloor(double value)
    {
        return MathHelper.floor_double_long(value);
    }

    public static float sqrt(double value)
    {
        return MathHelper.sqrt_double(value);
    }

    public static double clamp(double value, double min, double max)
    {
        return MathHelper.clamp_double(value, min, max);
    }

    public static int getInt(String valueString, int defaultValue)
    {
        return MathHelper.parseIntWithDefault(valueString, defaultValue);
    }

    public static double getDouble(String valueString, double defaultValue)
    {
        return MathHelper.parseDoubleWithDefault(valueString, defaultValue);
    }

    public static int log2(int value)
    {
        return MathHelper.calculateLogBaseTwo(value);
    }

    public static int absFloor(double value)
    {
        return MathHelper.absFloor(value);
    }

    public static int smallestEncompassingPowerOfTwo(int value)
    {
        return MathHelper.roundUpToPowerOfTwo(value);
    }

    public static float sqrt(float value)
    {
        return MathHelper.sqrt_float(value);
    }

    public static float cos(float value)
    {
        return MathHelper.cos(value);
    }

    public static int getInt(String valueString, int defaultValue, int min)
    {
        return MathHelper.parseIntWithDefaultAndMax(valueString, defaultValue, min);
    }

    public static int fastFloor(double value)
    {
        return MathHelper.truncateDoubleToInt(value);
    }

    public static double absMax(double a, double b)
    {
        return MathHelper.abs_max(a, b);
    }

    public static float nextFloat(Random random, float min, float max)
    {
        return MathHelper.randomFloatClamp(random, min, max);
    }

    public static double wrapDegrees(double degrees)
    {
        return MathHelper.wrapAngleTo180_double(degrees);
    }

    public static float wrapDegrees(float degrees)
    {
        return MathHelper.wrapAngleTo180_float(degrees);
    }

    public static float clamp(float value, float min, float max)
    {
        return MathHelper.clamp_float(value, min, max);
    }

    public static double getDouble(String valueString, double defaultValue, double min)
    {
        return MathHelper.parseDoubleWithDefaultAndMax(valueString, defaultValue, min);
    }

    public static int roundUp(int value, int interval)
    {
        return MathHelper.roundUp(value, interval);
    }

    public static double average(long[] values)
    {
        return MathHelper.average(values);
    }

    public static int floor(float value)
    {
        return MathHelper.floor_float(value);
    }

    public static int abs(int value)
    {
        return MathHelper.abs_int(value);
    }

    public static int nextInt(Random random, int min, int max)
    {
        return MathHelper.getRandomIntegerInRange(random, min, max);
    }
}
