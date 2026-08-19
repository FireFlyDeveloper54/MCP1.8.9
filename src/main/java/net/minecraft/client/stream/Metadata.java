package net.minecraft.client.stream;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import java.util.Map;

public class Metadata
{
    private static final Gson GSON = new Gson();
    private final String name;
    private String description;
    private Map<String, String> payload;

    public Metadata(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public Metadata(String name)
    {
        this(name, (String)null);
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return this.description == null ? this.name : this.description;
    }

    public void addPayloadEntry(String key, String value)
    {
        if (this.payload == null)
        {
            this.payload = Maps.<String, String>newHashMap();
        }

        if (this.payload.size() > 50)
        {
            throw new IllegalArgumentException("Metadata payload is full, cannot add more to it!");
        }
        else if (key == null)
        {
            throw new IllegalArgumentException("Metadata payload key cannot be null!");
        }
        else if (key.length() > 255)
        {
            throw new IllegalArgumentException("Metadata payload key is too long!");
        }
        else if (value == null)
        {
            throw new IllegalArgumentException("Metadata payload value cannot be null!");
        }
        else if (value.length() > 255)
        {
            throw new IllegalArgumentException("Metadata payload value is too long!");
        }
        else
        {
            this.payload.put(key, value);
        }
    }

    public String getPayloadJson()
    {
        return this.payload != null && !this.payload.isEmpty() ? GSON.toJson((Object)this.payload) : null;
    }

    public String getName()
    {
        return this.name;
    }

    public String toString()
    {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("description", this.description).add("data", this.getPayloadJson()).toString();
    }
}
