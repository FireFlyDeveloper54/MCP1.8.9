package net.minecraft.client.resources.model;

import java.util.Locale;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

public class ModelResourceLocation extends ResourceLocation
{
    private final String variant;
    private final int hashCode;

    protected ModelResourceLocation(int unused, String... pathParts)
    {
        super(0, new String[] {pathParts[0], pathParts[1]});
        this.variant = StringUtils.isEmpty(pathParts[2]) ? "normal" : pathParts[2].toLowerCase(Locale.ROOT);
        this.hashCode = 31 * super.hashCode() + this.variant.hashCode();
    }

    public ModelResourceLocation(String resourceName)
    {
        this(0, parsePathString(resourceName));
    }

    public ModelResourceLocation(ResourceLocation location, String variant)
    {
        this(location.toString(), variant);
    }

    public ModelResourceLocation(String resourceName, String variant)
    {
        this(0, parsePathString(resourceName + '#' + (variant == null ? "normal" : variant)));
    }

    protected static String[] parsePathString(String resourceName)
    {
        String[] pathParts = new String[] {null, resourceName, null};
        int variantSeparatorIndex = resourceName.indexOf(35);
        String pathWithoutVariant = resourceName;

        if (variantSeparatorIndex >= 0)
        {
            pathParts[2] = resourceName.substring(variantSeparatorIndex + 1, resourceName.length());

            if (variantSeparatorIndex > 1)
            {
                pathWithoutVariant = resourceName.substring(0, variantSeparatorIndex);
            }
        }

        System.arraycopy(ResourceLocation.splitObjectName(pathWithoutVariant), 0, pathParts, 0, 2);
        return pathParts;
    }

    public String getVariant()
    {
        return this.variant;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other instanceof ModelResourceLocation && super.equals(other))
        {
            ModelResourceLocation modelResourceLocation = (ModelResourceLocation)other;
            return this.variant.equals(modelResourceLocation.variant);
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return this.hashCode;
    }

    public String toString()
    {
        return super.toString() + '#' + this.variant;
    }
}
