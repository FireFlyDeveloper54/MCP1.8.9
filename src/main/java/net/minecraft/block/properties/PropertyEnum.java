package net.minecraft.block.properties;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import net.minecraft.util.IStringSerializable;

public class PropertyEnum<T extends Enum<T> & IStringSerializable> extends PropertyHelper<T>
{
    private final ImmutableSet<T> allowedValues;
    private final Map<String, T> valueByName = Maps.<String, T>newHashMap();

    protected PropertyEnum(String propertyName, Class<T> valueType, Collection<T> allowedValues)
    {
        super(propertyName, valueType);
        this.allowedValues = ImmutableSet.copyOf(allowedValues);

        for (T value : allowedValues)
        {
            String valueName = value.getName();

            if (this.valueByName.containsKey(valueName))
            {
                throw new IllegalArgumentException("Multiple values have the same name \'" + valueName + "\'");
            }

            this.valueByName.put(valueName, value);
        }
    }

    public Collection<T> getAllowedValues()
    {
        return this.allowedValues;
    }

    public String getName(T value)
    {
        return ((IStringSerializable)value).getName();
    }

    public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String propertyName, Class<T> valueType)
    {
        return create(propertyName, valueType, Predicates.<T>alwaysTrue());
    }

    public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String propertyName, Class<T> valueType, Predicate<T> filter)
    {
        return create(propertyName, valueType, Collections2.<T>filter(Lists.newArrayList(valueType.getEnumConstants()), filter));
    }

    public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String propertyName, Class<T> valueType, T... values)
    {
        return create(propertyName, valueType, Lists.newArrayList(values));
    }

    public static <T extends Enum<T> & IStringSerializable> PropertyEnum<T> create(String propertyName, Class<T> valueType, Collection<T> values)
    {
        return new PropertyEnum<T>(propertyName, valueType, values);
    }
}
