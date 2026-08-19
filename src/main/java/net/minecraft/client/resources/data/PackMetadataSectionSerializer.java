package net.minecraft.client.resources.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.JsonUtils;

public class PackMetadataSectionSerializer extends BaseMetadataSectionSerializer<PackMetadataSection> implements JsonSerializer<PackMetadataSection>
{
    public PackMetadataSection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonobject = json.getAsJsonObject();
        IChatComponent description = context.deserialize(jsonobject.get("description"), IChatComponent.class);

        if (description == null)
        {
            throw new JsonParseException("Invalid/missing description!");
        }
        else
        {
            int packFormat = JsonUtils.getInt(jsonobject, "pack_format");
            return new PackMetadataSection(description, packFormat);
        }
    }

    public JsonElement serialize(PackMetadataSection section, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pack_format", (Number)Integer.valueOf(section.getPackFormat()));
        jsonObject.add("description", context.serialize(section.getPackDescription()));
        return jsonObject;
    }

    public String getSectionName()
    {
        return "pack";
    }
}
