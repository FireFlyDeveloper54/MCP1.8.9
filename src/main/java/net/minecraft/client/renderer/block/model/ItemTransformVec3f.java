package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.MathHelper;
import org.lwjgl.util.vector.Vector3f;

public class ItemTransformVec3f
{
    public static final ItemTransformVec3f DEFAULT = new ItemTransformVec3f(new Vector3f(), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
    public final Vector3f rotation;
    public final Vector3f translation;
    public final Vector3f scale;

    public ItemTransformVec3f(Vector3f rotation, Vector3f translation, Vector3f scale)
    {
        this.rotation = new Vector3f(rotation);
        this.translation = new Vector3f(translation);
        this.scale = new Vector3f(scale);
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other == null || this.getClass() != other.getClass())
        {
            return false;
        }
        else
        {
            ItemTransformVec3f otherTransform = (ItemTransformVec3f)other;
            return !this.rotation.equals(otherTransform.rotation) ? false : (!this.scale.equals(otherTransform.scale) ? false : this.translation.equals(otherTransform.translation));
        }
    }

    public int hashCode()
    {
        int hash = this.rotation.hashCode();
        hash = 31 * hash + this.translation.hashCode();
        hash = 31 * hash + this.scale.hashCode();
        return hash;
    }

    static class Deserializer implements JsonDeserializer<ItemTransformVec3f>
    {
        private static final Vector3f ROTATION_DEFAULT = new Vector3f(0.0F, 0.0F, 0.0F);
        private static final Vector3f TRANSLATION_DEFAULT = new Vector3f(0.0F, 0.0F, 0.0F);
        private static final Vector3f SCALE_DEFAULT = new Vector3f(1.0F, 1.0F, 1.0F);

        public ItemTransformVec3f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            Vector3f rotation = this.parseVector3f(jsonObject, "rotation", ROTATION_DEFAULT);
            Vector3f translation = this.parseVector3f(jsonObject, "translation", TRANSLATION_DEFAULT);
            translation.scale(0.0625F);
            translation.x = MathHelper.clamp_float(translation.x, -1.5F, 1.5F);
            translation.y = MathHelper.clamp_float(translation.y, -1.5F, 1.5F);
            translation.z = MathHelper.clamp_float(translation.z, -1.5F, 1.5F);
            Vector3f scale = this.parseVector3f(jsonObject, "scale", SCALE_DEFAULT);
            scale.x = MathHelper.clamp_float(scale.x, -4.0F, 4.0F);
            scale.y = MathHelper.clamp_float(scale.y, -4.0F, 4.0F);
            scale.z = MathHelper.clamp_float(scale.z, -4.0F, 4.0F);
            return new ItemTransformVec3f(rotation, translation, scale);
        }

        private Vector3f parseVector3f(JsonObject jsonObject, String key, Vector3f defaultValue)
        {
            if (!jsonObject.has(key))
            {
                return defaultValue;
            }
            else
            {
                JsonArray jsonArray = JsonUtils.getJsonArray(jsonObject, key);

                if (jsonArray.size() != 3)
                {
                    throw new JsonParseException("Expected 3 " + key + " values, found: " + jsonArray.size());
                }
                else
                {
                    float[] vectorValues = new float[3];

                    for (int valueIndex = 0; valueIndex < vectorValues.length; ++valueIndex)
                    {
                        vectorValues[valueIndex] = JsonUtils.getFloat(jsonArray.get(valueIndex), key + "[" + valueIndex + "]");
                    }

                    return new Vector3f(vectorValues[0], vectorValues[1], vectorValues[2]);
                }
            }
        }
    }
}
