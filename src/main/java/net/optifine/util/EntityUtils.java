package net.optifine.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.src.Config;

public class EntityUtils
{
    private static final Map<Class, Integer> mapIdByClass = new HashMap();
    private static final Map<String, Integer> mapIdByName = new HashMap();
    private static final Map<String, Class> mapClassByName = new HashMap();

    public static int getEntityIdByClass(Entity entity)
    {
        return entity == null ? -1 : getEntityIdByClass(entity.getClass());
    }

    public static int getEntityIdByClass(Class cls)
    {
        Integer integer = (Integer)mapIdByClass.get(cls);
        return integer == null ? -1 : integer.intValue();
    }

    public static int getEntityIdByName(String name)
    {
        Integer integer = (Integer)mapIdByName.get(name);
        return integer == null ? -1 : integer.intValue();
    }

    public static Class getEntityClassByName(String name)
    {
        Class entityClass = (Class)mapClassByName.get(name);
        return entityClass;
    }

    static
    {
        for (int entityId = 0; entityId < 1000; ++entityId)
        {
            Class entityClass = EntityList.getClassFromID(entityId);

            if (entityClass != null)
            {
                String entityName = EntityList.getStringFromID(entityId);

                if (entityName != null)
                {
                    if (mapIdByClass.containsKey(entityClass))
                    {
                        Config.warn("Duplicate entity class: " + entityClass + ", id1: " + mapIdByClass.get(entityClass) + ", id2: " + entityId);
                    }

                    if (mapIdByName.containsKey(entityName))
                    {
                        Config.warn("Duplicate entity name: " + entityName + ", id1: " + mapIdByName.get(entityName) + ", id2: " + entityId);
                    }

                    if (mapClassByName.containsKey(entityName))
                    {
                        Config.warn("Duplicate entity name: " + entityName + ", class1: " + mapClassByName.get(entityName) + ", class2: " + entityClass);
                    }

                    mapIdByClass.put(entityClass, Integer.valueOf(entityId));
                    mapIdByName.put(entityName, Integer.valueOf(entityId));
                    mapClassByName.put(entityName, entityClass);
                }
            }
        }
    }
}
