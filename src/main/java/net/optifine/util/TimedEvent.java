package net.optifine.util;

import java.util.HashMap;
import java.util.Map;

public class TimedEvent
{
    private static Map<String, Long> eventTimes = new HashMap();

    public static boolean isActive(String name, long timeIntervalMs)
    {
        synchronized (eventTimes)
        {
            long currentTime = System.currentTimeMillis();
            Long lastTime = (Long)eventTimes.get(name);

            if (lastTime == null)
            {
                lastTime = Long.valueOf(currentTime);
                eventTimes.put(name, lastTime);
            }

            long previousTime = lastTime.longValue();

            if (currentTime < previousTime + timeIntervalMs)
            {
                return false;
            }
            else
            {
                eventTimes.put(name, Long.valueOf(currentTime));
                return true;
            }
        }
    }
}
