package net.optifine.entity.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.config.ConnectedParser;
import net.optifine.entity.model.anim.ModelUpdater;
import net.optifine.entity.model.anim.ModelVariableUpdater;
import net.optifine.player.PlayerItemParser;
import net.optifine.util.Json;

public class CustomEntityModelParser
{
    public static final String ENTITY = "entity";
    public static final String TEXTURE = "texture";
    public static final String SHADOW_SIZE = "shadowSize";
    public static final String ITEM_TYPE = "type";
    public static final String ITEM_TEXTURE_SIZE = "textureSize";
    public static final String ITEM_USE_PLAYER_TEXTURE = "usePlayerTexture";
    public static final String ITEM_MODELS = "models";
    public static final String ITEM_ANIMATIONS = "animations";
    public static final String MODEL_ID = "id";
    public static final String MODEL_BASE_ID = "baseId";
    public static final String MODEL_MODEL = "model";
    public static final String MODEL_TYPE = "type";
    public static final String MODEL_PART = "part";
    public static final String MODEL_ATTACH = "attach";
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
    public static final String ENTITY_MODEL = "EntityModel";
    public static final String ENTITY_MODEL_PART = "EntityModelPart";

    public static CustomEntityRenderer parseEntityRender(JsonObject obj, String path)
    {
        ConnectedParser connectedParser = new ConnectedParser("CustomEntityModels");
        String name = connectedParser.parseName(path);
        String basePath = connectedParser.parseBasePath(path);
        String texture = Json.getString(obj, "texture");
        int[] textureSize = Json.parseIntArray(obj.get("textureSize"), 2);
        float shadowSize = Json.getFloat(obj, "shadowSize", -1.0F);
        JsonArray models = (JsonArray)obj.get("models");
        checkNull(models, "Missing models");
        Map modelJsons = new HashMap();
        List customModelRenderers = new ArrayList();

        for (int modelIndex = 0; modelIndex < models.size(); ++modelIndex)
        {
            JsonObject modelObject = (JsonObject)models.get(modelIndex);
            processBaseId(modelObject, modelJsons);
            processExternalModel(modelObject, modelJsons, basePath);
            processId(modelObject, modelJsons);
            CustomModelRenderer customModelRenderer = parseCustomModelRenderer(modelObject, textureSize, basePath);

            if (customModelRenderer != null)
            {
                customModelRenderers.add(customModelRenderer);
            }
        }

        CustomModelRenderer[] modelRenderers = (CustomModelRenderer[])((CustomModelRenderer[])customModelRenderers.toArray(new CustomModelRenderer[customModelRenderers.size()]));
        ResourceLocation textureLocation = null;

        if (texture != null)
        {
            textureLocation = getResourceLocation(basePath, texture, ".png");
        }

        CustomEntityRenderer customEntityRenderer = new CustomEntityRenderer(name, basePath, textureLocation, modelRenderers, shadowSize);
        return customEntityRenderer;
    }

    private static void processBaseId(JsonObject elem, Map mapModelJsons)
    {
        String baseId = Json.getString(elem, "baseId");

        if (baseId != null)
        {
            JsonObject baseObject = (JsonObject)mapModelJsons.get(baseId);

            if (baseObject == null)
            {
                Config.warn("BaseID not found: " + baseId);
            }
            else
            {
                copyJsonElements(baseObject, elem);
            }
        }
    }

    private static void processExternalModel(JsonObject elem, Map mapModelJsons, String basePath)
    {
        String modelPath = Json.getString(elem, "model");

        if (modelPath != null)
        {
            ResourceLocation modelLocation = getResourceLocation(basePath, modelPath, ".jpm");

            try
            {
                JsonObject modelObject = loadJson(modelLocation);

                if (modelObject == null)
                {
                    Config.warn("Model not found: " + modelLocation);
                    return;
                }

                copyJsonElements(modelObject, elem);
            }
            catch (IOException ioException)
            {
                Config.error("" + ioException.getClass().getName() + ": " + ioException.getMessage());
            }
            catch (JsonParseException jsonParseException)
            {
                Config.error("" + jsonParseException.getClass().getName() + ": " + jsonParseException.getMessage());
            }
            catch (Exception exception)
            {
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            }
        }
    }

    private static void copyJsonElements(JsonObject objFrom, JsonObject objTo)
    {
        for (Entry<String, JsonElement> entry : objFrom.entrySet())
        {
            if (!((String)entry.getKey()).equals("id") && !objTo.has((String)entry.getKey()))
            {
                objTo.add((String)entry.getKey(), (JsonElement)entry.getValue());
            }
        }
    }

    public static ResourceLocation getResourceLocation(String basePath, String path, String extension)
    {
        if (!path.endsWith(extension))
        {
            path = path + extension;
        }

        if (!path.contains("/"))
        {
            path = basePath + "/" + path;
        }
        else if (path.startsWith("./"))
        {
            path = basePath + "/" + path.substring(2);
        }
        else if (path.startsWith("~/"))
        {
            path = "optifine/" + path.substring(2);
        }

        return new ResourceLocation(path);
    }

    private static void processId(JsonObject elem, Map mapModelJsons)
    {
        String modelId = Json.getString(elem, "id");

        if (modelId != null)
        {
            if (modelId.length() < 1)
            {
                Config.warn("Empty model ID: " + modelId);
            }
            else if (mapModelJsons.containsKey(modelId))
            {
                Config.warn("Duplicate model ID: " + modelId);
            }
            else
            {
                mapModelJsons.put(modelId, elem);
            }
        }
    }

    public static CustomModelRenderer parseCustomModelRenderer(JsonObject elem, int[] textureSize, String basePath)
    {
        String modelPart = Json.getString(elem, "part");
        checkNull(modelPart, "Model part not specified, missing \"replace\" or \"attachTo\".");
        boolean attach = Json.getBoolean(elem, "attach", false);
        ModelBase modelBase = new CustomEntityModel();

        if (textureSize != null)
        {
            modelBase.textureWidth = textureSize[0];
            modelBase.textureHeight = textureSize[1];
        }

        ModelUpdater modelUpdater = null;
        JsonArray animations = (JsonArray)elem.get("animations");

        if (animations != null)
        {
            List<ModelVariableUpdater> modelVariableUpdaters = new ArrayList();

            for (int animationIndex = 0; animationIndex < animations.size(); ++animationIndex)
            {
                JsonObject animationObject = (JsonObject)animations.get(animationIndex);

                for (Entry<String, JsonElement> entry : animationObject.entrySet())
                {
                    String variableName = (String)entry.getKey();
                    String expressionText = ((JsonElement)entry.getValue()).getAsString();
                    ModelVariableUpdater modelVariableUpdater = new ModelVariableUpdater(variableName, expressionText);
                    modelVariableUpdaters.add(modelVariableUpdater);
                }
            }

            if (modelVariableUpdaters.size() > 0)
            {
                ModelVariableUpdater[] modelVariableUpdaterArray = (ModelVariableUpdater[])((ModelVariableUpdater[])modelVariableUpdaters.toArray(new ModelVariableUpdater[modelVariableUpdaters.size()]));
                modelUpdater = new ModelUpdater(modelVariableUpdaterArray);
            }
        }

        ModelRenderer modelRenderer = PlayerItemParser.parseModelRenderer(elem, modelBase, textureSize, basePath);
        CustomModelRenderer customModelRenderer = new CustomModelRenderer(modelPart, attach, modelRenderer, modelUpdater);
        return customModelRenderer;
    }

    private static void checkNull(Object obj, String msg)
    {
        if (obj == null)
        {
            throw new JsonParseException(msg);
        }
    }

    public static JsonObject loadJson(ResourceLocation location) throws IOException, JsonParseException
    {
        InputStream inputStream = Config.getResourceStream(location);

        if (inputStream == null)
        {
            return null;
        }
        else
        {
            String jsonText = Config.readInputStream(inputStream, "ASCII");
            inputStream.close();
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject)jsonParser.parse(jsonText);
            return jsonObject;
        }
    }
}
