package net.minecraft.client.resources.data;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import net.minecraft.util.JsonUtils;

public class TextureMetadataSectionSerializer extends BaseMetadataSectionSerializer<TextureMetadataSection>
{
    public TextureMetadataSection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonobject = json.getAsJsonObject();
        boolean blur = JsonUtils.getBoolean(jsonobject, "blur", false);
        boolean clamp = JsonUtils.getBoolean(jsonobject, "clamp", false);
        List<Integer> mipmapLevels = Lists.<Integer>newArrayList();

        if (jsonobject.has("mipmaps"))
        {
            try
            {
                JsonArray jsonarray = jsonobject.getAsJsonArray("mipmaps");

                for (int mipmapIndex = 0; mipmapIndex < jsonarray.size(); ++mipmapIndex)
                {
                    JsonElement jsonelement = jsonarray.get(mipmapIndex);

                    if (jsonelement.isJsonPrimitive())
                    {
                        try
                        {
                            mipmapLevels.add(Integer.valueOf(jsonelement.getAsInt()));
                        }
                        catch (NumberFormatException numberformatexception)
                        {
                            throw new JsonParseException("Invalid texture->mipmap->" + mipmapIndex + ": expected number, was " + jsonelement, numberformatexception);
                        }
                    }
                    else if (jsonelement.isJsonObject())
                    {
                        throw new JsonParseException("Invalid texture->mipmap->" + mipmapIndex + ": expected number, was " + jsonelement);
                    }
                }
            }
            catch (ClassCastException classcastexception)
            {
                throw new JsonParseException("Invalid texture->mipmaps: expected array, was " + jsonobject.get("mipmaps"), classcastexception);
            }
        }

        return new TextureMetadataSection(blur, clamp, mipmapLevels);
    }

    public String getSectionName()
    {
        return "texture";
    }
}
