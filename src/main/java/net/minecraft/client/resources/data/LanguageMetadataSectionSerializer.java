package net.minecraft.client.resources.data;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.resources.Language;
import net.minecraft.util.JsonUtils;

public class LanguageMetadataSectionSerializer extends BaseMetadataSectionSerializer<LanguageMetadataSection>
{
    public LanguageMetadataSection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonobject = json.getAsJsonObject();
        Set<Language> languages = Sets.<Language>newHashSet();

        for (Entry<String, JsonElement> entry : jsonobject.entrySet())
        {
            String languageCode = (String)entry.getKey();
            JsonObject jsonobject1 = JsonUtils.getJsonObject((JsonElement)entry.getValue(), "language");
            String region = JsonUtils.getString(jsonobject1, "region");
            String name = JsonUtils.getString(jsonobject1, "name");
            boolean bidirectional = JsonUtils.getBoolean(jsonobject1, "bidirectional", false);

            if (region.isEmpty())
            {
                throw new JsonParseException("Invalid language->\'" + languageCode + "\'->region: empty value");
            }

            if (name.isEmpty())
            {
                throw new JsonParseException("Invalid language->\'" + languageCode + "\'->name: empty value");
            }

            if (!languages.add(new Language(languageCode, region, name, bidirectional)))
            {
                throw new JsonParseException("Duplicate language->\'" + languageCode + "\' defined");
            }
        }

        return new LanguageMetadataSection(languages);
    }

    public String getSectionName()
    {
        return "language";
    }
}
