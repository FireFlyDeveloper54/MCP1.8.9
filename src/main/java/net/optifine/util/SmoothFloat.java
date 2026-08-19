package net.optifine.util;

public class SmoothFloat
{
    private float lastValue;
    private float fadeUpSeconds;
    private float fadeDownSeconds;
    private long lastUpdateMs;

    public SmoothFloat(float valueLast, float timeFadeSec)
    {
        this(valueLast, timeFadeSec, timeFadeSec);
    }

    public SmoothFloat(float valueLast, float timeFadeUpSec, float timeFadeDownSec)
    {
        this.lastValue = valueLast;
        this.fadeUpSeconds = timeFadeUpSec;
        this.fadeDownSeconds = timeFadeDownSec;
        this.lastUpdateMs = System.currentTimeMillis();
    }

    public float getValueLast()
    {
        return this.lastValue;
    }

    public float getTimeFadeUpSec()
    {
        return this.fadeUpSeconds;
    }

    public float getTimeFadeDownSec()
    {
        return this.fadeDownSeconds;
    }

    public long getTimeLastMs()
    {
        return this.lastUpdateMs;
    }

    public float getSmoothValue(float value, float timeFadeUpSec, float timeFadeDownSec)
    {
        this.fadeUpSeconds = timeFadeUpSec;
        this.fadeDownSeconds = timeFadeDownSec;
        return this.getSmoothValue(value);
    }

    public float getSmoothValue(float value)
    {
        long currentTime = System.currentTimeMillis();
        float previousValue = this.lastValue;
        long previousTime = this.lastUpdateMs;
        float elapsedSeconds = (float)(currentTime - previousTime) / 1000.0F;
        float fadeSeconds = value >= previousValue ? this.fadeUpSeconds : this.fadeDownSeconds;
        float smoothValue = getSmoothValue(previousValue, value, elapsedSeconds, fadeSeconds);
        this.lastValue = smoothValue;
        this.lastUpdateMs = currentTime;
        return smoothValue;
    }

    public static float getSmoothValue(float valPrev, float value, float timeDeltaSec, float timeFadeSec)
    {
        if (timeDeltaSec <= 0.0F)
        {
            return valPrev;
        }
        else
        {
            float delta = value - valPrev;
            float result;

            if (timeFadeSec > 0.0F && timeDeltaSec < timeFadeSec && Math.abs(delta) > 1.0E-6F)
            {
                float fadeRatio = timeFadeSec / timeDeltaSec;
                float base = 4.61F;
                float offset = 0.13F;
                float scale = 10.0F;
                float curve = base - 1.0F / (offset + fadeRatio / scale);
                float blend = timeDeltaSec / timeFadeSec * curve;
                blend = NumUtils.limit(blend, 0.0F, 1.0F);
                result = valPrev + delta * blend;
            }
            else
            {
                result = value;
            }

            return result;
        }
    }
}
