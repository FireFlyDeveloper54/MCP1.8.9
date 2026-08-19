package net.minecraft.client.resources.data;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.Validate;

public class AnimationMetadataSectionSerializer extends BaseMetadataSectionSerializer<AnimationMetadataSection> implements JsonSerializer<AnimationMetadataSection>
{
    public AnimationMetadataSection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        List<AnimationFrame> animationFrames = Lists.<AnimationFrame>newArrayList();
        JsonObject jsonobject = JsonUtils.getJsonObject(json, "metadata section");
        int defaultFrameTime = JsonUtils.getInt(jsonobject, "frametime", 1);

        if (defaultFrameTime != 1)
        {
            Validate.inclusiveBetween(1L, 2147483647L, (long)defaultFrameTime, "Invalid default frame time");
        }

        if (jsonobject.has("frames"))
        {
            try
            {
                JsonArray jsonarray = JsonUtils.getJsonArray(jsonobject, "frames");

                for (int frameIndex = 0; frameIndex < jsonarray.size(); ++frameIndex)
                {
                    JsonElement jsonelement = jsonarray.get(frameIndex);
                    AnimationFrame animationframe = this.parseAnimationFrame(frameIndex, jsonelement);

                    if (animationframe != null)
                    {
                        animationFrames.add(animationframe);
                    }
                }
            }
            catch (ClassCastException classcastexception)
            {
                throw new JsonParseException("Invalid animation->frames: expected array, was " + jsonobject.get("frames"), classcastexception);
            }
        }

        int frameWidth = JsonUtils.getInt(jsonobject, "width", -1);
        int frameHeight = JsonUtils.getInt(jsonobject, "height", -1);

        if (frameWidth != -1)
        {
            Validate.inclusiveBetween(1L, 2147483647L, (long)frameWidth, "Invalid width");
        }

        if (frameHeight != -1)
        {
            Validate.inclusiveBetween(1L, 2147483647L, (long)frameHeight, "Invalid height");
        }

        boolean interpolate = JsonUtils.getBoolean(jsonobject, "interpolate", false);
        return new AnimationMetadataSection(animationFrames, frameWidth, frameHeight, defaultFrameTime, interpolate);
    }

    private AnimationFrame parseAnimationFrame(int frame, JsonElement json)
    {
        if (json.isJsonPrimitive())
        {
            return new AnimationFrame(JsonUtils.getInt(json, "frames[" + frame + "]"));
        }
        else if (json.isJsonObject())
        {
            JsonObject jsonObject = JsonUtils.getJsonObject(json, "frames[" + frame + "]");
            int frameTime = JsonUtils.getInt(jsonObject, "time", -1);

            if (jsonObject.has("time"))
            {
                Validate.inclusiveBetween(1L, 2147483647L, (long)frameTime, "Invalid frame time");
            }

            int frameIndex = JsonUtils.getInt(jsonObject, "index");
            Validate.inclusiveBetween(0L, 2147483647L, (long)frameIndex, "Invalid frame index");
            return new AnimationFrame(frameIndex, frameTime);
        }
        else
        {
            return null;
        }
    }

    public JsonElement serialize(AnimationMetadataSection section, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("frametime", (Number)Integer.valueOf(section.getFrameTime()));

        if (section.getFrameWidth() != -1)
        {
            jsonObject.addProperty("width", (Number)Integer.valueOf(section.getFrameWidth()));
        }

        if (section.getFrameHeight() != -1)
        {
            jsonObject.addProperty("height", (Number)Integer.valueOf(section.getFrameHeight()));
        }

        if (section.getFrameCount() > 0)
        {
            JsonArray jsonArray = new JsonArray();

            for (int frameNumber = 0; frameNumber < section.getFrameCount(); ++frameNumber)
            {
                if (section.frameHasTime(frameNumber))
                {
                    JsonObject jsonobject1 = new JsonObject();
                    jsonobject1.addProperty("index", (Number)Integer.valueOf(section.getFrameIndex(frameNumber)));
                    jsonobject1.addProperty("time", (Number)Integer.valueOf(section.getFrameTimeSingle(frameNumber)));
                    jsonArray.add(jsonobject1);
                }
                else
                {
                    jsonArray.add(new JsonPrimitive(Integer.valueOf(section.getFrameIndex(frameNumber))));
                }
            }

            jsonObject.add("frames", jsonArray);
        }

        return jsonObject;
    }

    public String getSectionName()
    {
        return "animation";
    }
}
