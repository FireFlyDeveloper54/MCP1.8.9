package net.minecraft.entity.ai.attributes;

public abstract class BaseAttribute implements IAttribute
{
    private final IAttribute parentAttribute;
    private final String unlocalizedName;
    private final double defaultValue;
    private final int cachedHashCode;
    private boolean shouldWatch;

    protected BaseAttribute(IAttribute parentAttributeIn, String unlocalizedNameIn, double defaultValueIn)
    {
        this.parentAttribute = parentAttributeIn;
        this.unlocalizedName = unlocalizedNameIn;
        this.defaultValue = defaultValueIn;

        if (unlocalizedNameIn == null)
        {
            throw new IllegalArgumentException("Name cannot be null!");
        }

        this.cachedHashCode = unlocalizedNameIn.hashCode();
    }

    public String getAttributeUnlocalizedName()
    {
        return this.unlocalizedName;
    }

    public double getDefaultValue()
    {
        return this.defaultValue;
    }

    public boolean getShouldWatch()
    {
        return this.shouldWatch;
    }

    public BaseAttribute setShouldWatch(boolean shouldWatchIn)
    {
        this.shouldWatch = shouldWatchIn;
        return this;
    }

    public IAttribute getParentAttribute()
    {
        return this.parentAttribute;
    }

    public int hashCode()
    {
        return this.cachedHashCode;
    }

    public boolean equals(Object other)
    {
        return other instanceof IAttribute && this.unlocalizedName.equals(((IAttribute)other).getAttributeUnlocalizedName());
    }
}
