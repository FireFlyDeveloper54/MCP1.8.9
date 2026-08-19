package net.optifine.util;

public class NumUtils
{
    public static float limit(float val, float min, float max)
    {
        return val < min ? min : (val > max ? max : val);
    }

    public static int mod(int x, int y)
    {
        int remainder = x % y;

        if (remainder < 0)
        {
            remainder += y;
        }

        return remainder;
    }
}
