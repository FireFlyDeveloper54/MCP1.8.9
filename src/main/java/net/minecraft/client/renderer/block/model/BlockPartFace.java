package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;

public class BlockPartFace
{
    public static final EnumFacing FACING_DEFAULT = null;
    public final EnumFacing cullFace;
    public final int tintIndex;
    public final String texture;
    public final BlockFaceUV blockFaceUV;

    public BlockPartFace(EnumFacing cullFaceIn, int tintIndexIn, String textureIn, BlockFaceUV blockFaceUVIn)
    {
        this.cullFace = cullFaceIn;
        this.tintIndex = tintIndexIn;
        this.texture = textureIn;
        this.blockFaceUV = blockFaceUVIn;
    }

    static class Deserializer implements JsonDeserializer<BlockPartFace>
    {
        public BlockPartFace deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            EnumFacing cullFace = this.parseCullFace(jsonObject);
            int tintIndex = this.parseTintIndex(jsonObject);
            String texture = this.parseTexture(jsonObject);
            BlockFaceUV blockFaceUV = context.deserialize(jsonObject, BlockFaceUV.class);
            return new BlockPartFace(cullFace, tintIndex, texture, blockFaceUV);
        }

        protected int parseTintIndex(JsonObject jsonObject)
        {
            return JsonUtils.getInt(jsonObject, "tintindex", -1);
        }

        private String parseTexture(JsonObject jsonObject)
        {
            return JsonUtils.getString(jsonObject, "texture");
        }

        private EnumFacing parseCullFace(JsonObject jsonObject)
        {
            String cullFaceName = JsonUtils.getString(jsonObject, "cullface", "");
            return EnumFacing.byName(cullFaceName);
        }
    }
}
