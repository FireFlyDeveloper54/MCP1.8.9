package net.minecraft.block.properties;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;

public class PropertyInteger extends PropertyHelper<Integer>
{
    private final ImmutableSet<Integer> allowedValues;
    private final int cachedHashCode;

    protected PropertyInteger(String name, int min, int max)
    {
        super(name, Integer.class);

        if (min < 0)
        {
            throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
        }
        else if (max <= min)
        {
            throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
        }
        else
        {
            Set<Integer> set = Sets.<Integer>newHashSet();

            for (int i = min; i <= max; ++i)
            {
                set.add(Integer.valueOf(i));
            }

            this.allowedValues = ImmutableSet.copyOf(set);
            this.cachedHashCode = 31 * super.hashCode() + this.allowedValues.hashCode();
        }
    }

    public Collection<Integer> getAllowedValues()
    {
        return this.allowedValues;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other != null && this.getClass() == other.getClass())
        {
            if (!super.equals(other))
            {
                return false;
            }
            else
            {
                PropertyInteger propertyInteger = (PropertyInteger)other;
                return this.allowedValues.equals(propertyInteger.allowedValues);
            }
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

    public static PropertyInteger create(String name, int min, int max)
    {
        return new PropertyInteger(name, min, max);
    }

    public String getName(Integer value)
    {
        return value.toString();
    }
}
