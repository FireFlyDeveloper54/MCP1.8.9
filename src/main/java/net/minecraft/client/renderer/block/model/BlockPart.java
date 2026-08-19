package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.MathHelper;
import org.lwjgl.util.vector.Vector3f;

public class BlockPart
{
    public final Vector3f positionFrom;
    public final Vector3f positionTo;
    public final Map<EnumFacing, BlockPartFace> mapFaces;
    public final BlockPartRotation partRotation;
    public final boolean shade;

    public BlockPart(Vector3f positionFromIn, Vector3f positionToIn, Map<EnumFacing, BlockPartFace> mapFacesIn, BlockPartRotation partRotationIn, boolean shadeIn)
    {
        this.positionFrom = positionFromIn;
        this.positionTo = positionToIn;
        this.mapFaces = mapFacesIn;
        this.partRotation = partRotationIn;
        this.shade = shadeIn;
        this.setDefaultUvs();
    }

    private void setDefaultUvs()
    {
        for (Entry<EnumFacing, BlockPartFace> entry : this.mapFaces.entrySet())
        {
            float[] faceUvs = this.getFaceUvs(entry.getKey());
            entry.getValue().blockFaceUV.setUvs(faceUvs);
        }
    }

    private float[] getFaceUvs(EnumFacing facing)
    {
        float[] faceUvs;

        switch (facing)
        {
            case DOWN:
            case UP:
                faceUvs = new float[] {this.positionFrom.x, this.positionFrom.z, this.positionTo.x, this.positionTo.z};
                break;
            case NORTH:
            case SOUTH:
                faceUvs = new float[] {this.positionFrom.x, 16.0F - this.positionTo.y, this.positionTo.x, 16.0F - this.positionFrom.y};
                break;
            case WEST:
            case EAST:
                faceUvs = new float[] {this.positionFrom.z, 16.0F - this.positionTo.y, this.positionTo.z, 16.0F - this.positionFrom.y};
                break;
            default:
                throw new NullPointerException();
        }

        return faceUvs;
    }

    static class Deserializer implements JsonDeserializer<BlockPart>
    {
        public BlockPart deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            Vector3f positionFrom = this.parsePositionFrom(jsonObject);
            Vector3f positionTo = this.parsePositionTo(jsonObject);
            BlockPartRotation partRotation = this.parseRotation(jsonObject);
            Map<EnumFacing, BlockPartFace> faces = this.parseFacesCheck(context, jsonObject);

            if (jsonObject.has("shade") && !JsonUtils.isBoolean(jsonObject, "shade"))
            {
                throw new JsonParseException("Expected shade to be a Boolean");
            }
            else
            {
                boolean shade = JsonUtils.getBoolean(jsonObject, "shade", true);
                return new BlockPart(positionFrom, positionTo, faces, partRotation, shade);
            }
        }

        private BlockPartRotation parseRotation(JsonObject jsonObject)
        {
            BlockPartRotation blockPartRotation = null;

            if (jsonObject.has("rotation"))
            {
                JsonObject rotationObject = JsonUtils.getJsonObject(jsonObject, "rotation");
                Vector3f origin = this.parsePosition(rotationObject, "origin");
                origin.scale(0.0625F);
                EnumFacing.Axis axis = this.parseAxis(rotationObject);
                float angle = this.parseAngle(rotationObject);
                boolean rescale = JsonUtils.getBoolean(rotationObject, "rescale", false);
                blockPartRotation = new BlockPartRotation(origin, axis, angle, rescale);
            }

            return blockPartRotation;
        }

        private float parseAngle(JsonObject jsonObject)
        {
            float angle = JsonUtils.getFloat(jsonObject, "angle");

            if (angle != 0.0F && MathHelper.abs(angle) != 22.5F && MathHelper.abs(angle) != 45.0F)
            {
                throw new JsonParseException("Invalid rotation " + angle + " found, only -45/-22.5/0/22.5/45 allowed");
            }
            else
            {
                return angle;
            }
        }

        private EnumFacing.Axis parseAxis(JsonObject jsonObject)
        {
            String axisName = JsonUtils.getString(jsonObject, "axis");
            EnumFacing.Axis axis = EnumFacing.Axis.byName(axisName.toLowerCase(Locale.ROOT));

            if (axis == null)
            {
                throw new JsonParseException("Invalid rotation axis: " + axisName);
            }
            else
            {
                return axis;
            }
        }

        private Map<EnumFacing, BlockPartFace> parseFacesCheck(JsonDeserializationContext context, JsonObject jsonObject)
        {
            Map<EnumFacing, BlockPartFace> faces = this.parseFaces(context, jsonObject);

            if (faces.isEmpty())
            {
                throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
            }
            else
            {
                return faces;
            }
        }

        private Map<EnumFacing, BlockPartFace> parseFaces(JsonDeserializationContext context, JsonObject jsonObject)
        {
            Map<EnumFacing, BlockPartFace> faces = Maps.newEnumMap(EnumFacing.class);
            JsonObject facesObject = JsonUtils.getJsonObject(jsonObject, "faces");

            for (Entry<String, JsonElement> entry : facesObject.entrySet())
            {
                EnumFacing facing = this.parseEnumFacing((String)entry.getKey());
                faces.put(facing, context.deserialize(entry.getValue(), BlockPartFace.class));
            }

            return faces;
        }

        private EnumFacing parseEnumFacing(String name)
        {
            EnumFacing facing = EnumFacing.byName(name);

            if (facing == null)
            {
                throw new JsonParseException("Unknown facing: " + name);
            }
            else
            {
                return facing;
            }
        }

        private Vector3f parsePositionTo(JsonObject jsonObject)
        {
            Vector3f positionTo = this.parsePosition(jsonObject, "to");

            if (positionTo.x >= -16.0F && positionTo.y >= -16.0F && positionTo.z >= -16.0F && positionTo.x <= 32.0F && positionTo.y <= 32.0F && positionTo.z <= 32.0F)
            {
                return positionTo;
            }
            else
            {
                throw new JsonParseException("\'to\' specifier exceeds the allowed boundaries: " + positionTo);
            }
        }

        private Vector3f parsePositionFrom(JsonObject jsonObject)
        {
            Vector3f positionFrom = this.parsePosition(jsonObject, "from");

            if (positionFrom.x >= -16.0F && positionFrom.y >= -16.0F && positionFrom.z >= -16.0F && positionFrom.x <= 32.0F && positionFrom.y <= 32.0F && positionFrom.z <= 32.0F)
            {
                return positionFrom;
            }
            else
            {
                throw new JsonParseException("\'from\' specifier exceeds the allowed boundaries: " + positionFrom);
            }
        }

        private Vector3f parsePosition(JsonObject jsonObject, String key)
        {
            JsonArray jsonArray = JsonUtils.getJsonArray(jsonObject, key);

            if (jsonArray.size() != 3)
            {
                throw new JsonParseException("Expected 3 " + key + " values, found: " + jsonArray.size());
            }
            else
            {
                float[] positionValues = new float[3];

                for (int valueIndex = 0; valueIndex < positionValues.length; ++valueIndex)
                {
                    positionValues[valueIndex] = JsonUtils.getFloat(jsonArray.get(valueIndex), key + "[" + valueIndex + "]");
                }

                return new Vector3f(positionValues[0], positionValues[1], positionValues[2]);
            }
        }
    }
}
