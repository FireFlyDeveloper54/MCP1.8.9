package optimization.betterfps;


public final class HybridMath
{
    private static final int FULL_BITS = 16;
    private static final int FULL_COUNT = 1 << FULL_BITS;
    private static final int FULL_MASK = FULL_COUNT - 1;
    private static final int COS_OFFSET = FULL_COUNT >> 2;

    private static final double RAD_TO_INDEX = FULL_COUNT / (Math.PI * 2.0);
    private static final double DEG_TO_INDEX = FULL_COUNT / 360.0;


    private static final float[] SIN = new float[FULL_COUNT + 1];


    private static final float[] DELTA = new float[FULL_COUNT];

    static
    {
        final double step = (Math.PI * 2.0) / (double)FULL_COUNT;

        for (int i = 0; i <= FULL_COUNT; i++)
        {
            SIN[i] = (float)Math.sin(i * step);
        }

        SIN[0] = 0.0F;
        SIN[COS_OFFSET] = 1.0F;
        SIN[COS_OFFSET * 2] = 0.0F;
        SIN[COS_OFFSET * 3] = -1.0F;
        SIN[FULL_COUNT] = 0.0F;

        for (int i = 0; i < FULL_COUNT; i++)
        {
            DELTA[i] = SIN[i + 1] - SIN[i];
        }
    }

    private HybridMath()
    {
    }

    public static float sin(float radians)
    {
        double scaled = (double)radians * RAD_TO_INDEX;
        int floor = fastFloor(scaled);
        float frac = (float)(scaled - (double)floor);
        int i = floor & FULL_MASK;
        return SIN[i] + DELTA[i] * frac;
    }

    public static float cos(float radians)
    {
        double scaled = (double)radians * RAD_TO_INDEX;
        int floor = fastFloor(scaled);
        float frac = (float)(scaled - (double)floor);
        int i = (floor + COS_OFFSET) & FULL_MASK;
        return SIN[i] + DELTA[i] * frac;
    }


    public static void sinCos(float radians, float[] out)
    {
        double scaled = (double)radians * RAD_TO_INDEX;
        int floor = fastFloor(scaled);
        float frac = (float)(scaled - (double)floor);

        int iSin = floor & FULL_MASK;
        int iCos = (floor + COS_OFFSET) & FULL_MASK;

        out[0] = SIN[iSin] + DELTA[iSin] * frac;
        out[1] = SIN[iCos] + DELTA[iCos] * frac;
    }


    public static float sinDeg(float degrees)
    {
        double scaled = (double)degrees * DEG_TO_INDEX;
        int floor = fastFloor(scaled);
        float frac = (float)(scaled - (double)floor);
        int i = floor & FULL_MASK;
        return SIN[i] + DELTA[i] * frac;
    }

    public static float cosDeg(float degrees)
    {
        double scaled = (double)degrees * DEG_TO_INDEX;
        int floor = fastFloor(scaled);
        float frac = (float)(scaled - (double)floor);
        int i = (floor + COS_OFFSET) & FULL_MASK;
        return SIN[i] + DELTA[i] * frac;
    }


    public static void sinCosDeg(float degrees, float[] out)
    {
        double scaled = (double)degrees * DEG_TO_INDEX;
        int floor = fastFloor(scaled);
        float frac = (float)(scaled - (double)floor);

        int iSin = floor & FULL_MASK;
        int iCos = (floor + COS_OFFSET) & FULL_MASK;

        out[0] = SIN[iSin] + DELTA[iSin] * frac;
        out[1] = SIN[iCos] + DELTA[iCos] * frac;
    }

    private static int fastFloor(double value)
    {
        int truncated = (int)value;
        return value < (double)truncated ? truncated - 1 : truncated;
    }
}
