package net.minecraft.client.resources.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.Validate;

public class FontMetadataSectionSerializer extends BaseMetadataSectionSerializer<FontMetadataSection>
{
    public FontMetadataSection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonobject = json.getAsJsonObject();
        float[] characterWidths = new float[256];
        float[] characterSpacings = new float[256];
        float[] characterLeftOffsets = new float[256];
        float defaultWidth = 1.0F;
        float defaultSpacing = 0.0F;
        float defaultLeftOffset = 0.0F;

        if (jsonobject.has("characters"))
        {
            if (!jsonobject.get("characters").isJsonObject())
            {
                throw new JsonParseException("Invalid font->characters: expected object, was " + jsonobject.get("characters"));
            }

            JsonObject jsonobject1 = jsonobject.getAsJsonObject("characters");

            if (jsonobject1.has("default"))
            {
                if (!jsonobject1.get("default").isJsonObject())
                {
                    throw new JsonParseException("Invalid font->characters->default: expected object, was " + jsonobject1.get("default"));
                }

                JsonObject jsonobject2 = jsonobject1.getAsJsonObject("default");
                defaultWidth = JsonUtils.getFloat(jsonobject2, "width", defaultWidth);
                Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)defaultWidth, "Invalid default width");
                defaultSpacing = JsonUtils.getFloat(jsonobject2, "spacing", defaultSpacing);
                Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)defaultSpacing, "Invalid default spacing");
                defaultLeftOffset = JsonUtils.getFloat(jsonobject2, "left", defaultLeftOffset);
                Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)defaultLeftOffset, "Invalid default left");
            }

            for (int characterIndex = 0; characterIndex < 256; ++characterIndex)
            {
                JsonElement jsonelement = jsonobject1.get(Integer.toString(characterIndex));
                float characterWidth = defaultWidth;
                float characterSpacing = defaultSpacing;
                float characterLeftOffset = defaultLeftOffset;

                if (jsonelement != null)
                {
                    JsonObject jsonobject3 = JsonUtils.getJsonObject(jsonelement, "characters[" + characterIndex + "]");
                    characterWidth = JsonUtils.getFloat(jsonobject3, "width", defaultWidth);
                    Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)characterWidth, "Invalid width");
                    characterSpacing = JsonUtils.getFloat(jsonobject3, "spacing", defaultSpacing);
                    Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)characterSpacing, "Invalid spacing");
                    characterLeftOffset = JsonUtils.getFloat(jsonobject3, "left", defaultLeftOffset);
                    Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)characterLeftOffset, "Invalid left");
                }

                characterWidths[characterIndex] = characterWidth;
                characterSpacings[characterIndex] = characterSpacing;
                characterLeftOffsets[characterIndex] = characterLeftOffset;
            }
        }

        return new FontMetadataSection(characterWidths, characterLeftOffsets, characterSpacings);
    }

    public String getSectionName()
    {
        return "font";
    }
}
