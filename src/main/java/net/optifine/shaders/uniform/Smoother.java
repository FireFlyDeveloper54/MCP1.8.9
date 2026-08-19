package net.optifine.shaders.uniform;

import java.util.HashMap;
import java.util.Map;
import net.optifine.util.CounterInt;
import net.optifine.util.SmoothFloat;

public class Smoother
{
    private static Map<Integer, SmoothFloat> mapSmoothValues = new HashMap();
    private static CounterInt counterIds = new CounterInt(1);

    public static float getSmoothValue(int id, float value, float timeFadeUpSec, float timeFadeDownSec)
    {
        synchronized (mapSmoothValues)
        {
            Integer integer = Integer.valueOf(id);
            SmoothFloat smoothFloat = (SmoothFloat)mapSmoothValues.get(integer);

            if (smoothFloat == null)
            {
                smoothFloat = new SmoothFloat(value, timeFadeUpSec, timeFadeDownSec);
                mapSmoothValues.put(integer, smoothFloat);
            }

            float smoothedValue = smoothFloat.getSmoothValue(value, timeFadeUpSec, timeFadeDownSec);
            return smoothedValue;
        }
    }

    public static int getNextId()
    {
        synchronized (counterIds)
        {
            return counterIds.nextValue();
        }
    }

    public static void resetValues()
    {
        synchronized (mapSmoothValues)
        {
            mapSmoothValues.clear();
        }
    }
}
