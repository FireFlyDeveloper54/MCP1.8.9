package net.minecraft.util;

public class LongHashMap<V>
{
    private transient LongHashMap.Entry<V>[] hashArray = new LongHashMap.Entry[4096];
    private transient int numHashElements;
    private int mask;
    private int capacity = 3072;
    private final float percentUseable = 0.75F;
    private transient volatile int modCount;

    public LongHashMap()
    {
        this.mask = this.hashArray.length - 1;
    }

    private static int getHashedKey(long originalKey)
    {
        return (int)(originalKey ^ originalKey >>> 27);
    }

    private static int hash(int integer)
    {
        integer = integer ^ integer >>> 20 ^ integer >>> 12;
        return integer ^ integer >>> 7 ^ integer >>> 4;
    }

    private static int getHashIndex(int hash, int mask)
    {
        return hash & mask;
    }

    public int getNumHashElements()
    {
        return this.numHashElements;
    }

    public V getValueByKey(long key)
    {
        int hashedKey = getHashedKey(key);

        for (LongHashMap.Entry<V> entry = this.hashArray[getHashIndex(hashedKey, this.mask)]; entry != null; entry = entry.nextEntry)
        {
            if (entry.key == key)
            {
                return entry.value;
            }
        }

        return (V)((Object)null);
    }

    public boolean containsItem(long key)
    {
        return this.getEntry(key) != null;
    }

    final LongHashMap.Entry<V> getEntry(long key)
    {
        int hashedKey = getHashedKey(key);

        for (LongHashMap.Entry<V> entry = this.hashArray[getHashIndex(hashedKey, this.mask)]; entry != null; entry = entry.nextEntry)
        {
            if (entry.key == key)
            {
                return entry;
            }
        }

        return null;
    }

    public void add(long key, V value)
    {
        int hashedKey = getHashedKey(key);
        int hashIndex = getHashIndex(hashedKey, this.mask);

        for (LongHashMap.Entry<V> entry = this.hashArray[hashIndex]; entry != null; entry = entry.nextEntry)
        {
            if (entry.key == key)
            {
                entry.value = value;
                return;
            }
        }

        ++this.modCount;
        this.createKey(hashedKey, key, value, hashIndex);
    }

    private void resizeTable(int newSize)
    {
        LongHashMap.Entry<V>[] oldHashArray = this.hashArray;
        int oldCapacity = oldHashArray.length;

        if (oldCapacity == 1073741824)
        {
            this.capacity = Integer.MAX_VALUE;
        }
        else
        {
            LongHashMap.Entry<V>[] newHashArray = new LongHashMap.Entry[newSize];
            this.copyHashTableTo(newHashArray);
            this.hashArray = newHashArray;
            this.mask = this.hashArray.length - 1;
            float newSizeFloat = (float)newSize;
            this.getClass();
            this.capacity = (int)(newSizeFloat * 0.75F);
        }
    }

    private void copyHashTableTo(LongHashMap.Entry<V>[] newHashArray)
    {
        LongHashMap.Entry<V>[] oldHashArray = this.hashArray;
        int newArrayLength = newHashArray.length;

        for (int oldIndex = 0; oldIndex < oldHashArray.length; ++oldIndex)
        {
            LongHashMap.Entry<V> currentEntry = oldHashArray[oldIndex];

            if (currentEntry != null)
            {
                oldHashArray[oldIndex] = null;

                while (true)
                {
                    LongHashMap.Entry<V> nextEntry = currentEntry.nextEntry;
                    int newHashIndex = getHashIndex(currentEntry.hash, newArrayLength - 1);
                    currentEntry.nextEntry = newHashArray[newHashIndex];
                    newHashArray[newHashIndex] = currentEntry;
                    currentEntry = nextEntry;

                    if (nextEntry == null)
                    {
                        break;
                    }
                }
            }
        }
    }

    public V remove(long key)
    {
        LongHashMap.Entry<V> entry = this.removeKey(key);
        return (V)(entry == null ? null : entry.value);
    }

    final LongHashMap.Entry<V> removeKey(long key)
    {
        int hashedKey = getHashedKey(key);
        int hashIndex = getHashIndex(hashedKey, this.mask);
        LongHashMap.Entry<V> previousEntry = this.hashArray[hashIndex];
        LongHashMap.Entry<V> currentEntry;
        LongHashMap.Entry<V> nextEntry;

        for (currentEntry = previousEntry; currentEntry != null; currentEntry = nextEntry)
        {
            nextEntry = currentEntry.nextEntry;

            if (currentEntry.key == key)
            {
                ++this.modCount;
                --this.numHashElements;

                if (previousEntry == currentEntry)
                {
                    this.hashArray[hashIndex] = nextEntry;
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

    private void createKey(int hash, long key, V value, int hashIndex)
    {
        LongHashMap.Entry<V> entry = this.hashArray[hashIndex];
        this.hashArray[hashIndex] = new LongHashMap.Entry(hash, key, value, entry);

        if (this.numHashElements++ >= this.capacity)
        {
            this.resizeTable(2 * this.hashArray.length);
        }
    }

    public double getKeyDistribution()
    {
        int occupiedBuckets = 0;

        for (int bucketIndex = 0; bucketIndex < this.hashArray.length; ++bucketIndex)
        {
            if (this.hashArray[bucketIndex] != null)
            {
                ++occupiedBuckets;
            }
        }

        return 1.0D * (double)occupiedBuckets / (double)this.numHashElements;
    }

    static class Entry<V>
    {
        final long key;
        V value;
        LongHashMap.Entry<V> nextEntry;
        final int hash;

        Entry(int hash, long key, V value, LongHashMap.Entry<V> nextEntry)
        {
            this.value = value;
            this.nextEntry = nextEntry;
            this.key = key;
            this.hash = hash;
        }

        public final long getKey()
        {
            return this.key;
        }

        public final V getValue()
        {
            return this.value;
        }

        public final boolean equals(Object objectIn)
        {
            if (!(objectIn instanceof LongHashMap.Entry))
            {
                return false;
            }
            else
            {
                LongHashMap.Entry<V> entry = (LongHashMap.Entry)objectIn;
                Object object = Long.valueOf(this.getKey());
                Object object1 = Long.valueOf(entry.getKey());

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
            return LongHashMap.getHashedKey(this.key);
        }

        public final String toString()
        {
            return this.getKey() + "=" + this.getValue();
        }
    }
}
