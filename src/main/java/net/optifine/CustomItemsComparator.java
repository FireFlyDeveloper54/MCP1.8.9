package net.optifine;

import java.util.Comparator;
import net.minecraft.src.Config;

public class CustomItemsComparator implements Comparator
{
    public int compare(Object object, Object secondObject)
    {
        CustomItemProperties customItemProperties = (CustomItemProperties)object;
        CustomItemProperties customitemproperties1 = (CustomItemProperties)secondObject;
        return customItemProperties.weight != customitemproperties1.weight ? customitemproperties1.weight - customItemProperties.weight : (!Config.equals(customItemProperties.basePath, customitemproperties1.basePath) ? customItemProperties.basePath.compareTo(customitemproperties1.basePath) : customItemProperties.name.compareTo(customitemproperties1.name));
    }
}
