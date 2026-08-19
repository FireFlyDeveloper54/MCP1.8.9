package net.minecraft.client.audio;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.Validate;

public class SoundListSerializer implements JsonDeserializer<SoundList>
{
    public SoundList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject jsonobject = JsonUtils.getJsonObject(json, "entry");
        SoundList soundlist = new SoundList();
        soundlist.setReplaceExisting(JsonUtils.getBoolean(jsonobject, "replace", false));
        SoundCategory soundcategory = SoundCategory.getCategory(JsonUtils.getString(jsonobject, "category", SoundCategory.MASTER.getCategoryName()));
        soundlist.setSoundCategory(soundcategory);
        Validate.notNull(soundcategory, "Invalid category", new Object[0]);

        if (jsonobject.has("sounds"))
        {
            JsonArray jsonarray = JsonUtils.getJsonArray(jsonobject, "sounds");

            for (int soundIndex = 0; soundIndex < jsonarray.size(); ++soundIndex)
            {
                JsonElement soundElement = jsonarray.get(soundIndex);
                SoundList.SoundEntry soundEntry = new SoundList.SoundEntry();

                if (JsonUtils.isString(soundElement))
                {
                    soundEntry.setSoundEntryName(JsonUtils.getString(soundElement, "sound"));
                }
                else
                {
                    JsonObject soundObject = JsonUtils.getJsonObject(soundElement, "sound");
                    soundEntry.setSoundEntryName(JsonUtils.getString(soundObject, "name"));

                    if (soundObject.has("type"))
                    {
                        SoundList.SoundEntry.Type soundType = SoundList.SoundEntry.Type.getType(JsonUtils.getString(soundObject, "type"));
                        Validate.notNull(soundType, "Invalid type", new Object[0]);
                        soundEntry.setSoundEntryType(soundType);
                    }

                    if (soundObject.has("volume"))
                    {
                        float soundVolume = JsonUtils.getFloat(soundObject, "volume");
                        Validate.isTrue(soundVolume > 0.0F, "Invalid volume", new Object[0]);
                        soundEntry.setSoundEntryVolume(soundVolume);
                    }

                    if (soundObject.has("pitch"))
                    {
                        float pitch = JsonUtils.getFloat(soundObject, "pitch");
                        Validate.isTrue(pitch > 0.0F, "Invalid pitch", new Object[0]);
                        soundEntry.setSoundEntryPitch(pitch);
                    }

                    if (soundObject.has("weight"))
                    {
                        int soundWeight = JsonUtils.getInt(soundObject, "weight");
                        Validate.isTrue(soundWeight > 0, "Invalid weight", new Object[0]);
                        soundEntry.setSoundEntryWeight(soundWeight);
                    }

                    if (soundObject.has("stream"))
                    {
                        soundEntry.setStreaming(JsonUtils.getBoolean(soundObject, "stream"));
                    }
                }

                soundlist.getSoundList().add(soundEntry);
            }
        }

        return soundlist;
    }
}
