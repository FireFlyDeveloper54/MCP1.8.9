package net.optifine.util;

import net.minecraft.util.MathHelper;

public class MathUtils
{
    public static final float PI = (float)Math.PI;
    public static final float PI2 = ((float)Math.PI * 2F);
    public static final float PId2 = ((float)Math.PI / 2F);
    private static final float[] ASIN_TABLE = new float[65536];

    public static float asin(float value)
    {
        return ASIN_TABLE[(int)((double)(value + 1.0F) * 32767.5D) & 65535];
    }

    public static float acos(float value)
    {
        return ((float)Math.PI / 2F) - ASIN_TABLE[(int)((double)(value + 1.0F) * 32767.5D) & 65535];
    }

    public static int getAverage(int[] vals)
    {
        if (vals.length <= 0)
        {
            return 0;
        }
        else
        {
            int sum = getSum(vals);
            int average = sum / vals.length;
            return average;
        }
    }

    public static int getSum(int[] vals)
    {
        if (vals.length <= 0)
        {
            return 0;
        }
        else
        {
            int sum = 0;

            for (int index = 0; index < vals.length; ++index)
            {
                int value = vals[index];
                sum += value;
            }

            return sum;
        }
    }

    public static int roundDownToPowerOfTwo(int val)
    {
        int roundedUp = MathHelper.roundUpToPowerOfTwo(val);
        return val == roundedUp ? roundedUp : roundedUp / 2;
    }

    public static boolean equalsDelta(float value1, float value2, float delta)
    {
        return Math.abs(value1 - value2) <= delta;
    }

    public static float toDeg(float angle)
    {
        return angle * 180.0F / MathHelper.PI;
    }

    public static float toRad(float angle)
    {
        return angle / 180.0F * MathHelper.PI;
    }

    public static float roundToFloat(double value)
    {
        return (float)((double)Math.round(value * 1.0E8D) / 1.0E8D);
    }

    static
    {
        for (int index = 0; index < 65536; ++index)
        {
            ASIN_TABLE[index] = (float)Math.asin((double)index / 32767.5D - 1.0D);
        }

        for (int value = -1; value < 2; ++value)
        {
            ASIN_TABLE[(int)(((double)value + 1.0D) * 32767.5D) & 65535] = (float)Math.asin((double)value);
        }
    }
}
