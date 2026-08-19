package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ModifiableAttributeInstance implements IAttributeInstance
{
    private final BaseAttributeMap attributeMap;
    private final IAttribute genericAttribute;
    private final Map<Integer, Set<AttributeModifier>> mapByOperation = Maps.<Integer, Set<AttributeModifier>>newHashMap();
    private final Map<String, Set<AttributeModifier>> mapByName = Maps.<String, Set<AttributeModifier>>newHashMap();
    private final Map<UUID, AttributeModifier> mapByUUID = Maps.<UUID, AttributeModifier>newHashMap();
    private double baseValue;
    private boolean needsUpdate = true;
    private double cachedValue;

    public ModifiableAttributeInstance(BaseAttributeMap attributeMapIn, IAttribute genericAttributeIn)
    {
        this.attributeMap = attributeMapIn;
        this.genericAttribute = genericAttributeIn;
        this.baseValue = genericAttributeIn.getDefaultValue();

        for (int operation = 0; operation < 3; ++operation)
        {
            this.mapByOperation.put(Integer.valueOf(operation), Sets.<AttributeModifier>newHashSet());
        }
    }

    public IAttribute getAttribute()
    {
        return this.genericAttribute;
    }

    public double getBaseValue()
    {
        return this.baseValue;
    }

    public void setBaseValue(double baseValue)
    {
        if (baseValue != this.getBaseValue())
        {
            this.baseValue = baseValue;
            this.flagForUpdate();
        }
    }

    public Collection<AttributeModifier> getModifiersByOperation(int operation)
    {
        return this.mapByOperation.get(Integer.valueOf(operation));
    }

    public Collection<AttributeModifier> getAllModifiers()
    {
        Set<AttributeModifier> allModifiers = Sets.<AttributeModifier>newHashSet();

        for (int operation = 0; operation < 3; ++operation)
        {
            allModifiers.addAll(this.getModifiersByOperation(operation));
        }

        return allModifiers;
    }

    public AttributeModifier getModifier(UUID uuid)
    {
        return this.mapByUUID.get(uuid);
    }

    public boolean hasModifier(AttributeModifier modifier)
    {
        return this.mapByUUID.get(modifier.getID()) != null;
    }

    public void applyModifier(AttributeModifier modifier)
    {
        if (this.getModifier(modifier.getID()) != null)
        {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        }
        else
        {
            Set<AttributeModifier> namedModifiers = this.mapByName.get(modifier.getName());

            if (namedModifiers == null)
            {
                namedModifiers = Sets.<AttributeModifier>newHashSet();
                this.mapByName.put(modifier.getName(), namedModifiers);
            }

            this.mapByOperation.get(Integer.valueOf(modifier.getOperation())).add(modifier);
            namedModifiers.add(modifier);
            this.mapByUUID.put(modifier.getID(), modifier);
            this.flagForUpdate();
        }
    }

    protected void flagForUpdate()
    {
        this.needsUpdate = true;
        this.attributeMap.onAttributeModified(this);
    }

    public void removeModifier(AttributeModifier modifier)
    {
        for (int operation = 0; operation < 3; ++operation)
        {
            Set<AttributeModifier> operationModifiers = this.mapByOperation.get(Integer.valueOf(operation));
            operationModifiers.remove(modifier);
        }

        Set<AttributeModifier> namedModifiers = this.mapByName.get(modifier.getName());

        if (namedModifiers != null)
        {
            namedModifiers.remove(modifier);

            if (namedModifiers.isEmpty())
            {
                this.mapByName.remove(modifier.getName());
            }
        }

        this.mapByUUID.remove(modifier.getID());
        this.flagForUpdate();
    }

    public void removeAllModifiers()
    {
        Collection<AttributeModifier> collection = this.getAllModifiers();

        if (collection != null)
        {
            for (AttributeModifier attributeModifier : Lists.newArrayList(collection))
            {
                this.removeModifier(attributeModifier);
            }
        }
    }

    public double getAttributeValue()
    {
        if (this.needsUpdate)
        {
            this.cachedValue = this.computeValue();
            this.needsUpdate = false;
        }

        return this.cachedValue;
    }

    private double computeValue()
    {
        double additiveValue = this.getBaseValue();

        for (AttributeModifier attributeModifier : this.getModifiersByOperationIncludingParents(0))
        {
            additiveValue += attributeModifier.getAmount();
        }

        double modifiedValue = additiveValue;

        for (AttributeModifier multiplyBaseModifier : this.getModifiersByOperationIncludingParents(1))
        {
            modifiedValue += additiveValue * multiplyBaseModifier.getAmount();
        }

        for (AttributeModifier multiplyTotalModifier : this.getModifiersByOperationIncludingParents(2))
        {
            modifiedValue *= 1.0D + multiplyTotalModifier.getAmount();
        }

        return this.genericAttribute.clampValue(modifiedValue);
    }

    private Collection<AttributeModifier> getModifiersByOperationIncludingParents(int operation)
    {
        Set<AttributeModifier> modifiers = Sets.newHashSet(this.getModifiersByOperation(operation));

        for (IAttribute parentAttribute = this.genericAttribute.getParentAttribute(); parentAttribute != null; parentAttribute = parentAttribute.getParentAttribute())
        {
            IAttributeInstance parentInstance = this.attributeMap.getAttributeInstance(parentAttribute);

            if (parentInstance != null)
            {
                modifiers.addAll(parentInstance.getModifiersByOperation(operation));
            }
        }

        return modifiers;
    }
}
