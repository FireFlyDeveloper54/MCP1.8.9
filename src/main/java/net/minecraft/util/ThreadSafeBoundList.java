package net.minecraft.util;

import java.lang.reflect.Array;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadSafeBoundList<T>
{
    private final T[] values;
    private final Class<? extends T> elementType;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private int size;
    private int nextIndex;

    public ThreadSafeBoundList(Class <? extends T > elementTypeIn, int capacity)
    {
        this.elementType = elementTypeIn;
        this.values = (T[])Array.newInstance(elementTypeIn, capacity);
    }

    public T add(T value)
    {
        this.lock.writeLock().lock();
        this.values[this.nextIndex] = value;
        this.nextIndex = (this.nextIndex + 1) % this.getCapacity();

        if (this.size < this.getCapacity())
        {
            ++this.size;
        }

        this.lock.writeLock().unlock();
        return value;
    }

    public int getCapacity()
    {
        this.lock.readLock().lock();
        int capacity = this.values.length;
        this.lock.readLock().unlock();
        return capacity;
    }

    public T[] toArray()
    {
        T[] copy = (T[])((Object[])Array.newInstance(this.elementType, this.size));
        this.lock.readLock().lock();

        for (int copyIndex = 0; copyIndex < this.size; ++copyIndex)
        {
            int valueIndex = (this.nextIndex - this.size + copyIndex) % this.getCapacity();

            if (valueIndex < 0)
            {
                valueIndex += this.getCapacity();
            }

            copy[copyIndex] = this.values[valueIndex];
        }

        this.lock.readLock().unlock();
        return copy;
    }
}
