package net.optifine.util;

public class MemoryMonitor
{
    private static long startTimeMs = System.currentTimeMillis();
    private static long startMemory = getMemoryUsed();
    private static long lastTimeMs = startTimeMs;
    private static long lastMemory = startMemory;
    private static boolean gcEvent = false;
    private static int memBytesSec = 0;
    private static long MB = 1048576L;

    public static void update()
    {
        long currentTimeMs = System.currentTimeMillis();
        long currentMemory = getMemoryUsed();
        gcEvent = currentMemory < lastMemory;

        if (gcEvent)
        {
            long elapsedTimeMs = lastTimeMs - startTimeMs;
            long allocatedBytes = lastMemory - startMemory;
            double elapsedSeconds = (double)elapsedTimeMs / 1000.0D;
            int bytesPerSecond = (int)((double)allocatedBytes / elapsedSeconds);

            if (bytesPerSecond > 0)
            {
                memBytesSec = bytesPerSecond;
            }

            startTimeMs = currentTimeMs;
            startMemory = currentMemory;
        }

        lastTimeMs = currentTimeMs;
        lastMemory = currentMemory;
    }

    private static long getMemoryUsed()
    {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static long getStartTimeMs()
    {
        return startTimeMs;
    }

    public static long getStartMemoryMb()
    {
        return startMemory / MB;
    }

    public static boolean isGcEvent()
    {
        return gcEvent;
    }

    public static long getAllocationRateMb()
    {
        return (long)memBytesSec / MB;
    }
}
