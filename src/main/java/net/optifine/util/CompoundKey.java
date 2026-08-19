package net.optifine.util;

import net.minecraft.src.Config;

public class CompoundKey
{
    private Object[] keys;
    private int cachedHashCode;

    public CompoundKey(Object[] keys)
    {
        this.cachedHashCode = 0;
        this.keys = (Object[])((Object[])keys.clone());
    }

    public CompoundKey(Object firstKey, Object secondKey)
    {
        this(new Object[] {firstKey, secondKey});
    }

    public CompoundKey(Object firstKey, Object secondKey, Object thirdKey)
    {
        this(new Object[] {firstKey, secondKey, thirdKey});
    }

    public int hashCode()
    {
        if (this.cachedHashCode == 0)
        {
            this.cachedHashCode = 7;

            for (int index = 0; index < this.keys.length; ++index)
            {
                Object key = this.keys[index];

                if (key != null)
                {
                    this.cachedHashCode = 31 * this.cachedHashCode + key.hashCode();
                }
            }
        }

        return this.cachedHashCode;
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof CompoundKey))
        {
            return false;
        }
        else
        {
            CompoundKey compoundKey = (CompoundKey)obj;
            Object[] otherKeys = compoundKey.getKeys();

            if (otherKeys.length != this.keys.length)
            {
                return false;
            }
            else
            {
                for (int index = 0; index < this.keys.length; ++index)
                {
                    if (!compareKeys(this.keys[index], otherKeys[index]))
                    {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    private static boolean compareKeys(Object object, Object secondObject)
    {
        return object == secondObject ? true : (object == null ? false : (secondObject == null ? false : object.equals(secondObject)));
    }

    private Object[] getKeys()
    {
        return this.keys;
    }

    public Object[] getKeysCopy()
    {
        return (Object[])((Object[])this.keys.clone());
    }

    public String toString()
    {
        return "[" + Config.arrayToString(this.keys) + "]";
    }
}
