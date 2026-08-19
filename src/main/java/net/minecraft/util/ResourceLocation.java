package net.minecraft.util;

import java.util.Locale;
import org.apache.commons.lang3.Validate;

public class ResourceLocation
{
    protected final String resourceDomain;
    protected final String resourcePath;
    private final int hashCode;

    protected ResourceLocation(int unused, String... resourceName)
    {
        this.resourceDomain = org.apache.commons.lang3.StringUtils.isEmpty(resourceName[0]) ? "minecraft" : resourceName[0].toLowerCase(Locale.ROOT);
        this.resourcePath = resourceName[1];
        Validate.notNull(this.resourcePath);
        this.hashCode = 31 * this.resourceDomain.hashCode() + this.resourcePath.hashCode();
    }

    public ResourceLocation(String resourceName)
    {
        this(0, splitObjectName(resourceName));
    }

    public ResourceLocation(String resourceDomainIn, String resourcePathIn)
    {
        this(0, new String[] {resourceDomainIn, resourcePathIn});
    }

    protected static String[] splitObjectName(String toSplit)
    {
        String[] astring = new String[] {null, toSplit};
        int separatorIndex = toSplit.indexOf(58);

        if (separatorIndex >= 0)
        {
            astring[1] = toSplit.substring(separatorIndex + 1, toSplit.length());

            if (separatorIndex > 1)
            {
                astring[0] = toSplit.substring(0, separatorIndex);
            }
        }

        return astring;
    }

    public String getResourcePath()
    {
        return this.resourcePath;
    }

    public String getResourceDomain()
    {
        return this.resourceDomain;
    }

    public String toString()
    {
        return this.resourceDomain + ':' + this.resourcePath;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof ResourceLocation))
        {
            return false;
        }
        else
        {
            ResourceLocation resourceLocation = (ResourceLocation)other;
            return this.resourceDomain.equals(resourceLocation.resourceDomain) && this.resourcePath.equals(resourceLocation.resourcePath);
        }
    }

    public int hashCode()
    {
        return this.hashCode;
    }
}
