package net.minecraft.client.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

public class SimpleResource implements IResource
{
    private final Map<String, IMetadataSection> mapMetadataSections = Maps.<String, IMetadataSection>newHashMap();
    private final String resourcePackName;
    private final ResourceLocation srResourceLocation;
    private final InputStream resourceInputStream;
    private final InputStream mcmetaInputStream;
    private final IMetadataSerializer srMetadataSerializer;
    private boolean mcmetaJsonChecked;
    private JsonObject mcmetaJson;

    public SimpleResource(String resourcePackNameIn, ResourceLocation srResourceLocationIn, InputStream resourceInputStreamIn, InputStream mcmetaInputStreamIn, IMetadataSerializer srMetadataSerializerIn)
    {
        this.resourcePackName = resourcePackNameIn;
        this.srResourceLocation = srResourceLocationIn;
        this.resourceInputStream = resourceInputStreamIn;
        this.mcmetaInputStream = mcmetaInputStreamIn;
        this.srMetadataSerializer = srMetadataSerializerIn;
    }

    public ResourceLocation getResourceLocation()
    {
        return this.srResourceLocation;
    }

    public InputStream getInputStream()
    {
        return this.resourceInputStream;
    }

    public boolean hasMetadata()
    {
        return this.mcmetaInputStream != null;
    }

    public <T extends IMetadataSection> T getMetadata(String metadataSectionName)
    {
        if (!this.hasMetadata())
        {
            return (T)null;
        }
        else
        {
            if (this.mcmetaJson == null && !this.mcmetaJsonChecked)
            {
                this.mcmetaJsonChecked = true;
                BufferedReader bufferedReader = null;

                try
                {
                    bufferedReader = new BufferedReader(new InputStreamReader(this.mcmetaInputStream));
                    this.mcmetaJson = (new JsonParser()).parse((Reader)bufferedReader).getAsJsonObject();
                }
                finally
                {
                    IOUtils.closeQuietly((Reader)bufferedReader);
                }
            }

            T t = (T)this.mapMetadataSections.get(metadataSectionName);

            if (t == null)
            {
                t = this.srMetadataSerializer.parseMetadataSection(metadataSectionName, this.mcmetaJson);
            }

            return t;
        }
    }

    public String getResourcePackName()
    {
        return this.resourcePackName;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof SimpleResource))
        {
            return false;
        }
        else
        {
            SimpleResource simpleResource = (SimpleResource)other;

            if (this.srResourceLocation != null)
            {
                if (!this.srResourceLocation.equals(simpleResource.srResourceLocation))
                {
                    return false;
                }
            }
            else if (simpleResource.srResourceLocation != null)
            {
                return false;
            }

            if (this.resourcePackName != null)
            {
                if (!this.resourcePackName.equals(simpleResource.resourcePackName))
                {
                    return false;
                }
            }
            else if (simpleResource.resourcePackName != null)
            {
                return false;
            }

            return true;
        }
    }

    public int hashCode()
    {
        int i = this.resourcePackName != null ? this.resourcePackName.hashCode() : 0;
        i = 31 * i + (this.srResourceLocation != null ? this.srResourceLocation.hashCode() : 0);
        return i;
    }
}
