package net.optifine.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.entity.model.CustomEntityModelParser;
import net.optifine.util.Json;

public class PlayerItemParser
{
    private static JsonParser jsonParser = new JsonParser();
    public static final String ITEM_TYPE = "type";
    public static final String ITEM_TEXTURE_SIZE = "textureSize";
    public static final String ITEM_USE_PLAYER_TEXTURE = "usePlayerTexture";
    public static final String ITEM_MODELS = "models";
    public static final String MODEL_ID = "id";
    public static final String MODEL_BASE_ID = "baseId";
    public static final String MODEL_TYPE = "type";
    public static final String MODEL_TEXTURE = "texture";
    public static final String MODEL_TEXTURE_SIZE = "textureSize";
    public static final String MODEL_ATTACH_TO = "attachTo";
    public static final String MODEL_INVERT_AXIS = "invertAxis";
    public static final String MODEL_MIRROR_TEXTURE = "mirrorTexture";
    public static final String MODEL_TRANSLATE = "translate";
    public static final String MODEL_ROTATE = "rotate";
    public static final String MODEL_SCALE = "scale";
    public static final String MODEL_BOXES = "boxes";
    public static final String MODEL_SPRITES = "sprites";
    public static final String MODEL_SUBMODEL = "submodel";
    public static final String MODEL_SUBMODELS = "submodels";
    public static final String BOX_TEXTURE_OFFSET = "textureOffset";
    public static final String BOX_COORDINATES = "coordinates";
    public static final String BOX_SIZE_ADD = "sizeAdd";
    public static final String BOX_UV_DOWN = "uvDown";
    public static final String BOX_UV_UP = "uvUp";
    public static final String BOX_UV_NORTH = "uvNorth";
    public static final String BOX_UV_SOUTH = "uvSouth";
    public static final String BOX_UV_WEST = "uvWest";
    public static final String BOX_UV_EAST = "uvEast";
    public static final String BOX_UV_FRONT = "uvFront";
    public static final String BOX_UV_BACK = "uvBack";
    public static final String BOX_UV_LEFT = "uvLeft";
    public static final String BOX_UV_RIGHT = "uvRight";
    public static final String ITEM_TYPE_MODEL = "PlayerItem";
    public static final String MODEL_TYPE_BOX = "ModelBox";

    public static PlayerItemModel parseItemModel(JsonObject obj)
    {
        String modelType = Json.getString(obj, "type");

        if (!Config.equals(modelType, "PlayerItem"))
        {
            throw new JsonParseException("Unknown model type: " + modelType);
        }
        else
        {
            int[] textureSize = Json.parseIntArray(obj.get("textureSize"), 2);
            checkNull(textureSize, "Missing texture size");
            Dimension textureDimension = new Dimension(textureSize[0], textureSize[1]);
            boolean usePlayerTexture = Json.getBoolean(obj, "usePlayerTexture", false);
            JsonArray modelArray = (JsonArray)obj.get("models");
            checkNull(modelArray, "Missing elements");
            Map modelMap = new HashMap();
            List itemRenderers = new ArrayList();
            new ArrayList();

            for (int modelIndex = 0; modelIndex < modelArray.size(); ++modelIndex)
            {
                JsonObject modelObject = (JsonObject)modelArray.get(modelIndex);
                String baseId = Json.getString(modelObject, "baseId");

                if (baseId != null)
                {
                    JsonObject baseModelObject = (JsonObject)modelMap.get(baseId);

                    if (baseModelObject == null)
                    {
                        Config.warn("BaseID not found: " + baseId);
                        continue;
                    }

                    for (Entry<String, JsonElement> entry : baseModelObject.entrySet())
                    {
                        if (!modelObject.has((String)entry.getKey()))
                        {
                            modelObject.add((String)entry.getKey(), (JsonElement)entry.getValue());
                        }
                    }
                }

                String modelId = Json.getString(modelObject, "id");

                if (modelId != null)
                {
                    if (!modelMap.containsKey(modelId))
                    {
                        modelMap.put(modelId, modelObject);
                    }
                    else
                    {
                        Config.warn("Duplicate model ID: " + modelId);
                    }
                }

                PlayerItemRenderer itemRenderer = parseItemRenderer(modelObject, textureDimension);

                if (itemRenderer != null)
                {
                    itemRenderers.add(itemRenderer);
                }
            }

            PlayerItemRenderer[] itemRendererArray = (PlayerItemRenderer[])((PlayerItemRenderer[])itemRenderers.toArray(new PlayerItemRenderer[itemRenderers.size()]));
            return new PlayerItemModel(textureDimension, usePlayerTexture, itemRendererArray);
        }
    }

    private static void checkNull(Object obj, String msg)
    {
        if (obj == null)
        {
            throw new JsonParseException(msg);
        }
    }

    private static ResourceLocation makeResourceLocation(String texture)
    {
        int colonIndex = texture.indexOf(58);

        if (colonIndex < 0)
        {
            return new ResourceLocation(texture);
        }
        else
        {
            String domain = texture.substring(0, colonIndex);
            String path = texture.substring(colonIndex + 1);
            return new ResourceLocation(domain, path);
        }
    }

    private static int parseAttachModel(String attachModelStr)
    {
        if (attachModelStr == null)
        {
            return 0;
        }
        else if (attachModelStr.equals("body"))
        {
            return 0;
        }
        else if (attachModelStr.equals("head"))
        {
            return 1;
        }
        else if (attachModelStr.equals("leftArm"))
        {
            return 2;
        }
        else if (attachModelStr.equals("rightArm"))
        {
            return 3;
        }
        else if (attachModelStr.equals("leftLeg"))
        {
            return 4;
        }
        else if (attachModelStr.equals("rightLeg"))
        {
            return 5;
        }
        else if (attachModelStr.equals("cape"))
        {
            return 6;
        }
        else
        {
            Config.warn("Unknown attachModel: " + attachModelStr);
            return 0;
        }
    }

    public static PlayerItemRenderer parseItemRenderer(JsonObject elem, Dimension textureDim)
    {
        String modelType = Json.getString(elem, "type");

        if (!Config.equals(modelType, "ModelBox"))
        {
            Config.warn("Unknown model type: " + modelType);
            return null;
        }
        else
        {
            String attachTo = Json.getString(elem, "attachTo");
            int attachModel = parseAttachModel(attachTo);
            ModelBase modelBase = new ModelPlayerItem();
            modelBase.textureWidth = textureDim.width;
            modelBase.textureHeight = textureDim.height;
            ModelRenderer modelRenderer = parseModelRenderer(elem, modelBase, (int[])null, (String)null);
            PlayerItemRenderer itemRenderer = new PlayerItemRenderer(attachModel, modelRenderer);
            return itemRenderer;
        }
    }

    public static ModelRenderer parseModelRenderer(JsonObject elem, ModelBase modelBase, int[] parentTextureSize, String basePath)
    {
        ModelRenderer modelRenderer = new ModelRenderer(modelBase);
        String rendererId = Json.getString(elem, "id");
        modelRenderer.setId(rendererId);
        float scale = Json.getFloat(elem, "scale", 1.0F);
        modelRenderer.scaleX = scale;
        modelRenderer.scaleY = scale;
        modelRenderer.scaleZ = scale;
        String texture = Json.getString(elem, "texture");

        if (texture != null)
        {
            modelRenderer.setTextureLocation(CustomEntityModelParser.getResourceLocation(basePath, texture, ".png"));
        }

        int[] modelTextureSize = Json.parseIntArray(elem.get("textureSize"), 2);

        if (modelTextureSize == null)
        {
            modelTextureSize = parentTextureSize;
        }

        if (modelTextureSize != null)
        {
            modelRenderer.setTextureSize(modelTextureSize[0], modelTextureSize[1]);
        }

        String invertAxis = Json.getString(elem, "invertAxis", "").toLowerCase();
        boolean invertX = invertAxis.contains("x");
        boolean invertY = invertAxis.contains("y");
        boolean invertZ = invertAxis.contains("z");
        float[] translation = Json.parseFloatArray(elem.get("translate"), 3, new float[3]);

        if (invertX)
        {
            translation[0] = -translation[0];
        }

        if (invertY)
        {
            translation[1] = -translation[1];
        }

        if (invertZ)
        {
            translation[2] = -translation[2];
        }

        float[] rotation = Json.parseFloatArray(elem.get("rotate"), 3, new float[3]);

        for (int axisIndex = 0; axisIndex < rotation.length; ++axisIndex)
        {
            rotation[axisIndex] = rotation[axisIndex] / 180.0F * MathHelper.PI;
        }

        if (invertX)
        {
            rotation[0] = -rotation[0];
        }

        if (invertY)
        {
            rotation[1] = -rotation[1];
        }

        if (invertZ)
        {
            rotation[2] = -rotation[2];
        }

        modelRenderer.setRotationPoint(translation[0], translation[1], translation[2]);
        modelRenderer.rotateAngleX = rotation[0];
        modelRenderer.rotateAngleY = rotation[1];
        modelRenderer.rotateAngleZ = rotation[2];
        String mirrorTexture = Json.getString(elem, "mirrorTexture", "").toLowerCase();
        boolean mirrorU = mirrorTexture.contains("u");
        boolean mirrorV = mirrorTexture.contains("v");

        if (mirrorU)
        {
            modelRenderer.mirror = true;
        }

        if (mirrorV)
        {
            modelRenderer.mirrorV = true;
        }

        JsonArray boxArray = elem.getAsJsonArray("boxes");

        if (boxArray != null)
        {
            for (int boxIndex = 0; boxIndex < boxArray.size(); ++boxIndex)
            {
                JsonObject boxObject = boxArray.get(boxIndex).getAsJsonObject();
                int[] boxTextureOffset = Json.parseIntArray(boxObject.get("textureOffset"), 2);
                int[][] faceUvs = parseFaceUvs(boxObject);

                if (boxTextureOffset == null && faceUvs == null)
                {
                    throw new JsonParseException("Texture offset not specified");
                }

                float[] coordinates = Json.parseFloatArray(boxObject.get("coordinates"), 6);

                if (coordinates == null)
                {
                    throw new JsonParseException("Coordinates not specified");
                }

                if (invertX)
                {
                    coordinates[0] = -coordinates[0] - coordinates[3];
                }

                if (invertY)
                {
                    coordinates[1] = -coordinates[1] - coordinates[4];
                }

                if (invertZ)
                {
                    coordinates[2] = -coordinates[2] - coordinates[5];
                }

                float sizeAdd = Json.getFloat(boxObject, "sizeAdd", 0.0F);

                if (faceUvs != null)
                {
                    modelRenderer.addBox(faceUvs, coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4], coordinates[5], sizeAdd);
                }
                else
                {
                    modelRenderer.setTextureOffset(boxTextureOffset[0], boxTextureOffset[1]);
                    modelRenderer.addBox(coordinates[0], coordinates[1], coordinates[2], (int)coordinates[3], (int)coordinates[4], (int)coordinates[5], sizeAdd);
                }
            }
        }

        JsonArray spriteArray = elem.getAsJsonArray("sprites");

        if (spriteArray != null)
        {
            for (int spriteIndex = 0; spriteIndex < spriteArray.size(); ++spriteIndex)
            {
                JsonObject spriteObject = spriteArray.get(spriteIndex).getAsJsonObject();
                int[] spriteTextureOffset = Json.parseIntArray(spriteObject.get("textureOffset"), 2);

                if (spriteTextureOffset == null)
                {
                    throw new JsonParseException("Texture offset not specified");
                }

                float[] spriteCoordinates = Json.parseFloatArray(spriteObject.get("coordinates"), 6);

                if (spriteCoordinates == null)
                {
                    throw new JsonParseException("Coordinates not specified");
                }

                if (invertX)
                {
                    spriteCoordinates[0] = -spriteCoordinates[0] - spriteCoordinates[3];
                }

                if (invertY)
                {
                    spriteCoordinates[1] = -spriteCoordinates[1] - spriteCoordinates[4];
                }

                if (invertZ)
                {
                    spriteCoordinates[2] = -spriteCoordinates[2] - spriteCoordinates[5];
                }

                float spriteSizeAdd = Json.getFloat(spriteObject, "sizeAdd", 0.0F);
                modelRenderer.setTextureOffset(spriteTextureOffset[0], spriteTextureOffset[1]);
                modelRenderer.addSprite(spriteCoordinates[0], spriteCoordinates[1], spriteCoordinates[2], (int)spriteCoordinates[3], (int)spriteCoordinates[4], (int)spriteCoordinates[5], spriteSizeAdd);
            }
        }

        JsonObject singleSubmodelObject = (JsonObject)elem.get("submodel");

        if (singleSubmodelObject != null)
        {
            ModelRenderer childRenderer = parseModelRenderer(singleSubmodelObject, modelBase, modelTextureSize, basePath);
            modelRenderer.addChild(childRenderer);
        }

        JsonArray submodelArray = (JsonArray)elem.get("submodels");

        if (submodelArray != null)
        {
            for (int submodelIndex = 0; submodelIndex < submodelArray.size(); ++submodelIndex)
            {
                JsonObject submodelObject = (JsonObject)submodelArray.get(submodelIndex);
                ModelRenderer submodelRenderer = parseModelRenderer(submodelObject, modelBase, modelTextureSize, basePath);

                if (submodelRenderer.getId() != null)
                {
                    ModelRenderer existingChild = modelRenderer.getChild(submodelRenderer.getId());

                    if (existingChild != null)
                    {
                        Config.warn("Duplicate model ID: " + submodelRenderer.getId());
                    }
                }

                modelRenderer.addChild(submodelRenderer);
            }
        }

        return modelRenderer;
    }

    private static int[][] parseFaceUvs(JsonObject box)
    {
        int[][] faceUvs = new int[][] {Json.parseIntArray(box.get("uvDown"), 4), Json.parseIntArray(box.get("uvUp"), 4), Json.parseIntArray(box.get("uvNorth"), 4), Json.parseIntArray(box.get("uvSouth"), 4), Json.parseIntArray(box.get("uvWest"), 4), Json.parseIntArray(box.get("uvEast"), 4)};

        if (faceUvs[2] == null)
        {
            faceUvs[2] = Json.parseIntArray(box.get("uvFront"), 4);
        }

        if (faceUvs[3] == null)
        {
            faceUvs[3] = Json.parseIntArray(box.get("uvBack"), 4);
        }

        if (faceUvs[4] == null)
        {
            faceUvs[4] = Json.parseIntArray(box.get("uvLeft"), 4);
        }

        if (faceUvs[5] == null)
        {
            faceUvs[5] = Json.parseIntArray(box.get("uvRight"), 4);
        }

        boolean hasFaceUvs = false;

        for (int faceIndex = 0; faceIndex < faceUvs.length; ++faceIndex)
        {
            if (faceUvs[faceIndex] != null)
            {
                hasFaceUvs = true;
            }
        }

        if (!hasFaceUvs)
        {
            return (int[][])null;
        }
        else
        {
            return faceUvs;
        }
    }
}
