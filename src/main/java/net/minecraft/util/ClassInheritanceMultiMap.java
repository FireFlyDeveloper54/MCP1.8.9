package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.optifine.util.IteratorCache;

public class ClassInheritanceMultiMap<T> extends AbstractSet<T>
{
    private static final Set<Class<?>> GLOBAL_LOOKUPS = Collections.<Class<?>>newSetFromMap(new ConcurrentHashMap());
    private final Map < Class<?>, List<T >> map = Maps. < Class<?>, List<T >> newHashMap();
    private final Set < Class<? >> knownKeys = Sets. < Class<? >> newIdentityHashSet();
    private final Class<T> baseClass;
    private final List<T> values = Lists.<T>newArrayList();
    public boolean empty;

    public ClassInheritanceMultiMap(Class<T> baseClassIn)
    {
        this.baseClass = baseClassIn;
        this.knownKeys.add(baseClassIn);
        this.map.put(baseClassIn, this.values);

        for (Class<?> oclass : GLOBAL_LOOKUPS)
        {
            this.createLookup(oclass);
        }

        this.empty = this.values.size() == 0;
    }

    protected void createLookup(Class<?> clazz)
    {
        GLOBAL_LOOKUPS.add(clazz);
        int valueCount = this.values.size();

        for (int valueIndex = 0; valueIndex < valueCount; ++valueIndex)
        {
            T value = this.values.get(valueIndex);

            if (clazz.isAssignableFrom(value.getClass()))
            {
                this.addForClass(value, clazz);
            }
        }

        this.knownKeys.add(clazz);
    }

    protected Class<?> initializeClassLookup(Class<?> clazz)
    {
        if (this.baseClass.isAssignableFrom(clazz))
        {
            if (!this.knownKeys.contains(clazz))
            {
                this.createLookup(clazz);
            }

            return clazz;
        }
        else
        {
            throw new IllegalArgumentException("Don\'t know how to search for " + clazz);
        }
    }

    public boolean add(T value)
    {
        for (Class<?> oclass : this.knownKeys)
        {
            if (oclass.isAssignableFrom(value.getClass()))
            {
                this.addForClass(value, oclass);
            }
        }

        this.empty = this.values.size() == 0;
        return true;
    }

    private void addForClass(T value, Class<?> parentClass)
    {
        List<T> list = this.map.get(parentClass);

        if (list == null)
        {
            this.map.put(parentClass, Lists.newArrayList(value));
        }
        else
        {
            list.add(value);
        }

        this.empty = this.values.size() == 0;
    }

    public boolean remove(Object object)
    {
        T value = (T)object;
        boolean removed = false;

        for (Class<?> oclass : this.knownKeys)
        {
            if (oclass.isAssignableFrom(value.getClass()))
            {
                List<T> list = this.map.get(oclass);

                if (list != null && list.remove(value))
                {
                    removed = true;
                }
            }
        }

        this.empty = this.values.size() == 0;
        return removed;
    }

    public boolean contains(Object object)
    {
        return Iterators.contains(this.getByClass(object.getClass()).iterator(), object);
    }

    public <S> Iterable<S> getByClass(final Class<S> clazz)
    {
        return new Iterable<S>()
        {
            public Iterator<S> iterator()
            {
                List<T> list = ClassInheritanceMultiMap.this.map.get(ClassInheritanceMultiMap.this.initializeClassLookup(clazz));

                if (list == null)
                {
                    return Collections.<S>emptyList().iterator();
                }
                else
                {
                    Iterator<T> iterator = list.iterator();
                    return Iterators.filter(iterator, clazz);
                }
            }
        };
    }

    public Iterator<T> iterator()
    {
        return (Iterator<T>)(this.values.isEmpty() ? Collections.<T>emptyList().iterator() : IteratorCache.getReadOnly(this.values));
    }

    public int size()
    {
        return this.values.size();
    }

    public boolean isEmpty()
    {
        return this.empty;
    }
}
