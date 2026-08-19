package net.minecraft.util;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Set;

public class JsonSerializableSet extends ForwardingSet<String> implements IJsonSerializable
{
    private final Set<String> underlyingSet = Sets.<String>newHashSet();

    public void fromJson(JsonElement json)
    {
        if (json.isJsonArray())
        {
            for (JsonElement jsonElement : json.getAsJsonArray())
            {
                this.add(jsonElement.getAsString());
            }
        }
    }

    public JsonElement getSerializableElement()
    {
        JsonArray jsonArray = new JsonArray();

        for (String value : this)
        {
            jsonArray.add(new JsonPrimitive(value));
        }

        return jsonArray;
    }

    protected Set<String> delegate()
    {
        return this.underlyingSet;
    }
}
