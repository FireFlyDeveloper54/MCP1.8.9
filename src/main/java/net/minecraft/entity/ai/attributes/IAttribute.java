package net.minecraft.entity.ai.attributes;

public interface IAttribute
{
    String getAttributeUnlocalizedName();

    double clampValue(double value);

    double getDefaultValue();

    boolean getShouldWatch();

    IAttribute getParentAttribute();
}
