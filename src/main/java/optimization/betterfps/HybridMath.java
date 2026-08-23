package optimization.betterfps;

/**
 * Hybrid LUT sin/cos — combines the useful parts of BetterFPS / engine tables:
 * <ul>
 *   <li>Vanilla-class resolution (16-bit full circle = 65536 bins)</li>
 *   <li>Linear interpolation via precomputed delta (one fewer load on hot path)</li>
 *   <li>Exact cardinals 0 / +/-1 (Rivens &amp; LibGDX trick)</li>
 *   <li>Dual-path cos via integer π/2 phase (no float phase add)</li>
 *   <li>Flat full-circle table (no quadrant switch)</li>
 *   <li>double-precision index mapping (large-angle float error stays low)</li>
 *   <li>Degree path that skips deg→rad conversion (MC yaw/pitch style)</li>
 * </ul>
 * Extra tables are built at class-init only (load-time cost, zero runtime cost).
 * Max abs error stays well below vanilla nearest-sample error.
 */
public final class HybridMath
{
    private static final int FULL_BITS = 16;
    private static final int FULL_COUNT = 1 << FULL_BITS;
    private static final int FULL_MASK = FULL_COUNT - 1;
    private static final int COS_OFFSET = FULL_COUNT >> 2;

    private static final double RAD_TO_INDEX = FULL_COUNT / (Math.PI * 2.0);
    private static final double DEG_TO_INDEX = FULL_COUNT / 360.0;

    /** Full-circle sin samples + wrap entry at {@code [FULL_COUNT]}. */
    private static final float[] SIN = new float[FULL_COUNT + 1];

    /**
     * {@code DELTA[i] = SIN[i + 1] - SIN[i]} — precomputed at load so lerp is
     * {@code SIN[i] + DELTA[i] * frac} (one table load instead of two + sub).
     */
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

    /**
     * Shared-index sin+cos. {@code out[0] = sin}, {@code out[1] = cos}.
     */
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

    /**
     * Degree-domain sin — same table, no deg→rad multiply.
     * Prefer for MC yaw/pitch style inputs already in degrees.
     */
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

    /**
     * Shared-index degree sin+cos. {@code out[0] = sin}, {@code out[1] = cos}.
     */
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
