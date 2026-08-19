package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.util.JsonUtils;

public class BlockFaceUV
{
    public float[] uvs;
    public final int rotation;

    public BlockFaceUV(float[] uvsIn, int rotationIn)
    {
        this.uvs = uvsIn;
        this.rotation = rotationIn;
    }

    public float getVertexU(int vertexIndex)
    {
        if (this.uvs == null)
        {
            throw new NullPointerException("uvs");
        }
        else
        {
            int rotatedVertexIndex = this.getRotatedVertexIndex(vertexIndex);
            return rotatedVertexIndex != 0 && rotatedVertexIndex != 1 ? this.uvs[2] : this.uvs[0];
        }
    }

    public float getVertexV(int vertexIndex)
    {
        if (this.uvs == null)
        {
            throw new NullPointerException("uvs");
        }
        else
        {
            int rotatedVertexIndex = this.getRotatedVertexIndex(vertexIndex);
            return rotatedVertexIndex != 0 && rotatedVertexIndex != 3 ? this.uvs[3] : this.uvs[1];
        }
    }

    private int getRotatedVertexIndex(int vertexIndex)
    {
        return (vertexIndex + this.rotation / 90) % 4;
    }

    public int getInverseRotatedVertexIndex(int vertexIndex)
    {
        return (vertexIndex + (4 - this.rotation / 90)) % 4;
    }

    public void setUvs(float[] uvsIn)
    {
        if (this.uvs == null)
        {
            this.uvs = uvsIn;
        }
    }

    static class Deserializer implements JsonDeserializer<BlockFaceUV>
    {
        public BlockFaceUV deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            float[] uvs = this.parseUV(jsonObject);
            int rotation = this.parseRotation(jsonObject);
            return new BlockFaceUV(uvs, rotation);
        }

        protected int parseRotation(JsonObject object)
        {
            int rotation = JsonUtils.getInt(object, "rotation", 0);

            if (rotation >= 0 && rotation % 90 == 0 && rotation / 90 <= 3)
            {
                return rotation;
            }
            else
            {
                throw new JsonParseException("Invalid rotation " + rotation + " found, only 0/90/180/270 allowed");
            }
        }

        private float[] parseUV(JsonObject object)
        {
            if (!object.has("uv"))
            {
                return null;
            }
            else
            {
                JsonArray jsonArray = JsonUtils.getJsonArray(object, "uv");

                if (jsonArray.size() != 4)
                {
                    throw new JsonParseException("Expected 4 uv values, found: " + jsonArray.size());
                }
                else
                {
                    float[] uvs = new float[4];

                    for (int uvIndex = 0; uvIndex < uvs.length; ++uvIndex)
                    {
                        uvs[uvIndex] = JsonUtils.getFloat(jsonArray.get(uvIndex), "uv[" + uvIndex + "]");
                    }

                    return uvs;
                }
            }
        }
    }
}
