package net.optifine.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class PropertiesOrdered extends Properties
{
    private Set<Object> orderedKeys = new LinkedHashSet();

    public synchronized Object put(Object key, Object value)
    {
        this.orderedKeys.add(key);
        return super.put(key, value);
    }

    public Set<Object> keySet()
    {
        Set<Object> keys = super.keySet();
        this.orderedKeys.retainAll(keys);
        return Collections.<Object>unmodifiableSet(this.orderedKeys);
    }

    public synchronized Enumeration<Object> keys()
    {
        return Collections.<Object>enumeration(this.keySet());
    }
}
