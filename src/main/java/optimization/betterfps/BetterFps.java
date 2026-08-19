package optimization.betterfps;

/**
 * BetterFPS-style sin/cos algorithms (from Guilherme Chaguri / Naven port),
 * plus a hybrid combiner that keeps the useful parts of each.
 * Default is {@link Mode#HYBRID}.
 * <p>
 * Hot path: when mode is {@link Mode#HYBRID} (the default), {@link #sin}/{@link #cos}
 * go straight to {@link HybridMath} with no enum switch — monomorphic for the JIT.
 * {@link net.minecraft.util.MathHelper} also short-circuits to {@link HybridMath}
 * when {@link #isHybridFastPath()} is true so the default game path pays zero
 * dispatcher overhead.
 */
public final class BetterFps
{
    public enum Mode
    {
        HYBRID,
        VANILLA,
        LIBGDX,
        RIVENS_FULL,
        RIVENS_HALF,
        RIVENS,
        JAVA
    }

    private static Mode mode = Mode.HYBRID;

    /**
     * Cached fast-path flag so the default HYBRID case does not pay an enum switch
     * on every sin/cos. Updated only in {@link #setMode(Mode)}.
     */
    private static boolean hybridFastPath = true;

    private BetterFps()
    {
    }

    public static Mode getMode()
    {
        return mode;
    }

    /** {@code true} when the default {@link Mode#HYBRID} path is active (no mode switch). */
    public static boolean isHybridFastPath()
    {
        return hybridFastPath;
    }

    public static void setMode(Mode newMode)
    {
        mode = newMode == null ? Mode.HYBRID : newMode;
        hybridFastPath = mode == Mode.HYBRID;
    }

    public static float sin(float radians)
    {
        if (hybridFastPath)
        {
            return HybridMath.sin(radians);
        }

        return sinSlow(radians);
    }

    public static float cos(float radians)
    {
        if (hybridFastPath)
        {
            return HybridMath.cos(radians);
        }

        return cosSlow(radians);
    }

    /**
     * Fills {@code out[0]=sin}, {@code out[1]=cos}. Hybrid uses a shared-index path;
     * other modes fall back to separate sin/cos calls.
     */
    public static void sinCos(float radians, float[] out)
    {
        if (hybridFastPath)
        {
            HybridMath.sinCos(radians, out);
            return;
        }

        out[0] = sinSlow(radians);
        out[1] = cosSlow(radians);
    }

    public static float sinDeg(float degrees)
    {
        if (hybridFastPath)
        {
            return HybridMath.sinDeg(degrees);
        }

        return sin(degrees * 0.017453292F);
    }

    public static float cosDeg(float degrees)
    {
        if (hybridFastPath)
        {
            return HybridMath.cosDeg(degrees);
        }

        return cos(degrees * 0.017453292F);
    }

    public static void sinCosDeg(float degrees, float[] out)
    {
        if (hybridFastPath)
        {
            HybridMath.sinCosDeg(degrees, out);
            return;
        }

        float radians = degrees * 0.017453292F;
        out[0] = sin(radians);
        out[1] = cos(radians);
    }

    private static float sinSlow(float radians)
    {
        switch (mode)
        {
            case JAVA:
                return JavaMath.sin(radians);

            case LIBGDX:
                return LibGDXMath.sin(radians);

            case RIVENS_FULL:
                return RivensFullMath.sin(radians);

            case RIVENS:
                return RivensMath.sin(radians);

            case RIVENS_HALF:
                return RivensHalfMath.sin(radians);

            case VANILLA:
                return VanillaMath.sin(radians);

            case HYBRID:
            default:
                return HybridMath.sin(radians);
        }
    }

    private static float cosSlow(float radians)
    {
        switch (mode)
        {
            case JAVA:
                return JavaMath.cos(radians);

            case LIBGDX:
                return LibGDXMath.cos(radians);

            case RIVENS_FULL:
                return RivensFullMath.cos(radians);

            case RIVENS:
                return RivensMath.cos(radians);

            case RIVENS_HALF:
                return RivensHalfMath.cos(radians);

            case VANILLA:
                return VanillaMath.cos(radians);

            case HYBRID:
            default:
                return HybridMath.cos(radians);
        }
    }
}
