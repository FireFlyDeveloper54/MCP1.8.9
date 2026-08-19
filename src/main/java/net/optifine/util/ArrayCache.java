package net.optifine.util;

import java.lang.reflect.Array;
import java.util.ArrayDeque;

public class ArrayCache
{
    private Class componentClass = null;
    private int maxCacheSize = 0;
    private ArrayDeque cachedArrays = new ArrayDeque();

    public ArrayCache(Class elementClass, int maxCacheSize)
    {
        this.componentClass = elementClass;
        this.maxCacheSize = maxCacheSize;
    }

    public synchronized Object allocate(int size)
    {
        Object cachedArray = this.cachedArrays.pollLast();

        if (cachedArray == null || Array.getLength(cachedArray) < size)
        {
            cachedArray = Array.newInstance(this.componentClass, size);
        }

        return cachedArray;
    }

    public synchronized void free(Object array)
    {
        if (array != null)
        {
            Class arrayClass = array.getClass();

            if (arrayClass.getComponentType() != this.componentClass)
            {
                throw new IllegalArgumentException("Wrong component type");
            }
            else if (this.cachedArrays.size() < this.maxCacheSize)
            {
                this.cachedArrays.add(array);
            }
        }
    }
}
