package net.minecraft.util;

import java.util.Arrays;

public class IntHashMap<V>
{
    private transient IntHashMap.Entry<V>[] slots = new IntHashMap.Entry[16];
    private transient int count;
    private int threshold = 12;
    private final float growFactor = 0.75F;

    private static int computeHash(int value)
    {
        value = value ^ value >>> 20 ^ value >>> 12;
        return value ^ value >>> 7 ^ value >>> 4;
    }

    private static int getSlotIndex(int hash, int slotCount)
    {
        return hash & slotCount - 1;
    }

    public V lookup(int key)
    {
        int hash = computeHash(key);

        for (IntHashMap.Entry<V> entry = this.slots[getSlotIndex(hash, this.slots.length)]; entry != null; entry = entry.nextEntry)
        {
            if (entry.hashEntry == key)
            {
                return entry.valueEntry;
            }
        }

        return (V)null;
    }

    public boolean containsItem(int key)
    {
        return this.lookupEntry(key) != null;
    }

    final IntHashMap.Entry<V> lookupEntry(int key)
    {
        int hash = computeHash(key);

        for (IntHashMap.Entry<V> entry = this.slots[getSlotIndex(hash, this.slots.length)]; entry != null; entry = entry.nextEntry)
        {
            if (entry.hashEntry == key)
            {
                return entry;
            }
        }

        return null;
    }

    public void addKey(int key, V value)
    {
        int hash = computeHash(key);
        int slotIndex = getSlotIndex(hash, this.slots.length);

        for (IntHashMap.Entry<V> entry = this.slots[slotIndex]; entry != null; entry = entry.nextEntry)
        {
            if (entry.hashEntry == key)
            {
                entry.valueEntry = value;
                return;
            }
        }

        this.insert(hash, key, value, slotIndex);
    }

    private void grow(int newCapacity)
    {
        IntHashMap.Entry<V>[] currentSlots = this.slots;
        int slotCount = currentSlots.length;

        if (slotCount == 1073741824)
        {
            this.threshold = Integer.MAX_VALUE;
        }
        else
        {
            IntHashMap.Entry<V>[] newSlots = new IntHashMap.Entry[newCapacity];
            this.copyTo(newSlots);
            this.slots = newSlots;
            this.threshold = (int)((float)newCapacity * this.growFactor);
        }
    }

    private void copyTo(IntHashMap.Entry<V>[] newSlots)
    {
        IntHashMap.Entry<V>[] oldSlots = this.slots;
        int newSlotCount = newSlots.length;

        for (int slotIndex = 0; slotIndex < oldSlots.length; ++slotIndex)
        {
            IntHashMap.Entry<V> entry = oldSlots[slotIndex];

            if (entry != null)
            {
                oldSlots[slotIndex] = null;

                while (true)
                {
                    IntHashMap.Entry<V> nextEntry = entry.nextEntry;
                    int newSlotIndex = getSlotIndex(entry.slotHash, newSlotCount);
                    entry.nextEntry = newSlots[newSlotIndex];
                    newSlots[newSlotIndex] = entry;
                    entry = nextEntry;

                    if (nextEntry == null)
                    {
                        break;
                    }
                }
            }
        }
    }

    public V removeObject(int key)
    {
        IntHashMap.Entry<V> entry = this.removeEntry(key);
        return (V)(entry == null ? null : entry.valueEntry);
    }

    final IntHashMap.Entry<V> removeEntry(int key)
    {
        int hash = computeHash(key);
        int slotIndex = getSlotIndex(hash, this.slots.length);
        IntHashMap.Entry<V> previousEntry = this.slots[slotIndex];
        IntHashMap.Entry<V> currentEntry;
        IntHashMap.Entry<V> nextEntry;

        for (currentEntry = previousEntry; currentEntry != null; currentEntry = nextEntry)
        {
            nextEntry = currentEntry.nextEntry;

            if (currentEntry.hashEntry == key)
            {
                --this.count;

                if (previousEntry == currentEntry)
                {
                    this.slots[slotIndex] = nextEntry;
                }
                else
                {
                    previousEntry.nextEntry = nextEntry;
                }

                return currentEntry;
            }

            previousEntry = currentEntry;
        }

        return currentEntry;
    }

    public void clearMap()
    {
        Arrays.fill(this.slots, null);
        this.count = 0;
    }

    private void insert(int hash, int key, V value, int slotIndex)
    {
        IntHashMap.Entry<V> entry = this.slots[slotIndex];
        this.slots[slotIndex] = new IntHashMap.Entry(hash, key, value, entry);

        if (this.count++ >= this.threshold)
        {
            this.grow(2 * this.slots.length);
        }
    }

    static class Entry<V>
    {
        final int hashEntry;
        V valueEntry;
        IntHashMap.Entry<V> nextEntry;
        final int slotHash;

        Entry(int slotHash, int key, V value, IntHashMap.Entry<V> nextEntry)
        {
            this.valueEntry = value;
            this.nextEntry = nextEntry;
            this.hashEntry = key;
            this.slotHash = slotHash;
        }

        public final int getHash()
        {
            return this.hashEntry;
        }

        public final V getValue()
        {
            return this.valueEntry;
        }

        public final boolean equals(Object other)
        {
            if (!(other instanceof IntHashMap.Entry))
            {
                return false;
            }
            else
            {
                IntHashMap.Entry<V> entry = (IntHashMap.Entry)other;
                Object object = Integer.valueOf(this.getHash());
                Object object1 = Integer.valueOf(entry.getHash());

                if (object == object1 || object != null && object.equals(object1))
                {
                    Object object2 = this.getValue();
                    Object object3 = entry.getValue();

                    if (object2 == object3 || object2 != null && object2.equals(object3))
                    {
                        return true;
                    }
                }

                return false;
            }
        }

        public final int hashCode()
        {
            return IntHashMap.computeHash(this.hashEntry);
        }

        public final String toString()
        {
            return this.getHash() + "=" + this.getValue();
        }
    }
}
