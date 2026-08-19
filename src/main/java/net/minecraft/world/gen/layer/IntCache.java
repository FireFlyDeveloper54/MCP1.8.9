package net.minecraft.world.gen.layer;

import com.google.common.collect.Lists;
import java.util.List;

public class IntCache
{
    private static int intCacheSize = 256;
    private static List<int[]> freeSmallArrays = Lists.<int[]>newArrayList();
    private static List<int[]> inUseSmallArrays = Lists.<int[]>newArrayList();
    private static List<int[]> freeLargeArrays = Lists.<int[]>newArrayList();
    private static List<int[]> inUseLargeArrays = Lists.<int[]>newArrayList();

    public static synchronized int[] getIntCache(int requestedSize)
    {
        if (requestedSize <= 256)
        {
            if (freeSmallArrays.isEmpty())
            {
                int[] newSmallArray = new int[256];
                inUseSmallArrays.add(newSmallArray);
                return newSmallArray;
            }
            else
            {
                int[] reusedSmallArray = (int[])freeSmallArrays.remove(freeSmallArrays.size() - 1);
                inUseSmallArrays.add(reusedSmallArray);
                return reusedSmallArray;
            }
        }
        else if (requestedSize > intCacheSize)
        {
            intCacheSize = requestedSize;
            freeLargeArrays.clear();
            inUseLargeArrays.clear();
            int[] newLargeArray = new int[intCacheSize];
            inUseLargeArrays.add(newLargeArray);
            return newLargeArray;
        }
        else if (freeLargeArrays.isEmpty())
        {
            int[] newLargeArray = new int[intCacheSize];
            inUseLargeArrays.add(newLargeArray);
            return newLargeArray;
        }
        else
        {
            int[] reusedLargeArray = (int[])freeLargeArrays.remove(freeLargeArrays.size() - 1);
            inUseLargeArrays.add(reusedLargeArray);
            return reusedLargeArray;
        }
    }

    public static synchronized void resetIntCache()
    {
        if (!freeLargeArrays.isEmpty())
        {
            freeLargeArrays.remove(freeLargeArrays.size() - 1);
        }

        if (!freeSmallArrays.isEmpty())
        {
            freeSmallArrays.remove(freeSmallArrays.size() - 1);
        }

        freeLargeArrays.addAll(inUseLargeArrays);
        freeSmallArrays.addAll(inUseSmallArrays);
        inUseLargeArrays.clear();
        inUseSmallArrays.clear();
    }

    public static synchronized String getCacheSizes()
    {
        return "cache: " + freeLargeArrays.size() + ", tcache: " + freeSmallArrays.size() + ", allocated: " + inUseLargeArrays.size() + ", tallocated: " + inUseSmallArrays.size();
    }
}
