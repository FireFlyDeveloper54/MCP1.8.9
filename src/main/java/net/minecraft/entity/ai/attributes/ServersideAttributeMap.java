package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.management.LowerStringMap;

public class ServersideAttributeMap extends BaseAttributeMap
{
    private final Set<IAttributeInstance> attributeInstanceSet = Sets.<IAttributeInstance>newHashSet();
    protected final Map<String, IAttributeInstance> descriptionToAttributeInstanceMap = new LowerStringMap();

    public ModifiableAttributeInstance getAttributeInstance(IAttribute attribute)
    {
        return (ModifiableAttributeInstance)super.getAttributeInstance(attribute);
    }

    public ModifiableAttributeInstance getAttributeInstanceByName(String attributeName)
    {
        IAttributeInstance iattributeinstance = super.getAttributeInstanceByName(attributeName);

        if (iattributeinstance == null)
        {
            iattributeinstance = (IAttributeInstance)this.descriptionToAttributeInstanceMap.get(attributeName);
        }

        return (ModifiableAttributeInstance)iattributeinstance;
    }

    public IAttributeInstance registerAttribute(IAttribute attribute)
    {
        IAttributeInstance iattributeinstance = super.registerAttribute(attribute);

        if (attribute instanceof RangedAttribute && ((RangedAttribute)attribute).getDescription() != null)
        {
            this.descriptionToAttributeInstanceMap.put(((RangedAttribute)attribute).getDescription(), iattributeinstance);
        }

        return iattributeinstance;
    }

    protected IAttributeInstance createInstance(IAttribute attribute)
    {
        return new ModifiableAttributeInstance(this, attribute);
    }

    public void onAttributeModified(IAttributeInstance instance)
    {
        if (instance.getAttribute().getShouldWatch())
        {
            this.attributeInstanceSet.add(instance);
        }

        for (IAttribute iattribute : this.descendantsByParent.get(instance.getAttribute()))
        {
            ModifiableAttributeInstance modifiableAttributeInstance = this.getAttributeInstance(iattribute);

            if (modifiableAttributeInstance != null)
            {
                modifiableAttributeInstance.flagForUpdate();
            }
        }
    }

    public Set<IAttributeInstance> getAttributeInstanceSet()
    {
        return this.attributeInstanceSet;
    }

    public Collection<IAttributeInstance> getWatchedAttributes()
    {
        Set<IAttributeInstance> set = Sets.<IAttributeInstance>newHashSet();

        for (IAttributeInstance iattributeinstance : this.getAllAttributes())
        {
            if (iattributeinstance.getAttribute().getShouldWatch())
            {
                set.add(iattributeinstance);
            }
        }

        return set;
    }
}
