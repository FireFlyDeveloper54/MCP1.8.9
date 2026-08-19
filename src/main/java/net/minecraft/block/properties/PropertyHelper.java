package net.minecraft.block.properties;

import com.google.common.base.MoreObjects;

public abstract class PropertyHelper<T extends Comparable<T>> implements IProperty<T>
{
    private final Class<T> valueType;
    private final String name;
    private final int cachedHashCode;

    protected PropertyHelper(String propertyName, Class<T> valueType)
    {
        this.valueType = valueType;
        this.name = propertyName;
        this.cachedHashCode = 31 * this.valueType.hashCode() + this.name.hashCode();
    }

    public String getName()
    {
        return this.name;
    }

    public Class<T> getValueClass()
    {
        return this.valueType;
    }

    public String toString()
    {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.valueType).add("values", this.getAllowedValues()).toString();
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other != null && this.getClass() == other.getClass())
        {
            PropertyHelper propertyHelper = (PropertyHelper)other;
            return this.valueType.equals(propertyHelper.valueType) && this.name.equals(propertyHelper.name);
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return this.cachedHashCode;
    }
}
