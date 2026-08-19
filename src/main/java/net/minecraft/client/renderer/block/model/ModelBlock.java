package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModelBlock
{
    private static final Logger LOGGER = LogManager.getLogger();
    static final Gson SERIALIZER = (new GsonBuilder()).registerTypeAdapter(ModelBlock.class, new ModelBlock.Deserializer()).registerTypeAdapter(BlockPart.class, new BlockPart.Deserializer()).registerTypeAdapter(BlockPartFace.class, new BlockPartFace.Deserializer()).registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer()).registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer()).registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransforms.Deserializer()).create();
    private final List<BlockPart> elements;
    private final boolean gui3d;
    private final boolean ambientOcclusion;
    private ItemCameraTransforms cameraTransforms;
    public String name;
    public final Map<String, String> textures;
    protected ModelBlock parent;
    public ResourceLocation parentLocation;

    public static ModelBlock deserialize(Reader readerIn)
    {
        return SERIALIZER.fromJson(readerIn, ModelBlock.class);
    }

    public static ModelBlock deserialize(String jsonString)
    {
        return deserialize(new StringReader(jsonString));
    }

    protected ModelBlock(List<BlockPart> elementsIn, Map<String, String> texturesIn, boolean ambientOcclusionIn, boolean gui3dIn, ItemCameraTransforms cameraTransformsIn)
    {
        this((ResourceLocation)null, elementsIn, texturesIn, ambientOcclusionIn, gui3dIn, cameraTransformsIn);
    }

    protected ModelBlock(ResourceLocation parentLocationIn, Map<String, String> texturesIn, boolean ambientOcclusionIn, boolean gui3dIn, ItemCameraTransforms cameraTransformsIn)
    {
        this(parentLocationIn, Collections.<BlockPart>emptyList(), texturesIn, ambientOcclusionIn, gui3dIn, cameraTransformsIn);
    }

    private ModelBlock(ResourceLocation parentLocationIn, List<BlockPart> elementsIn, Map<String, String> texturesIn, boolean ambientOcclusionIn, boolean gui3dIn, ItemCameraTransforms cameraTransformsIn)
    {
        this.name = "";
        this.elements = elementsIn;
        this.ambientOcclusion = ambientOcclusionIn;
        this.gui3d = gui3dIn;
        this.textures = texturesIn;
        this.parentLocation = parentLocationIn;
        this.cameraTransforms = cameraTransformsIn;
    }

    public List<BlockPart> getElements()
    {
        return this.hasParent() ? this.parent.getElements() : this.elements;
    }

    private boolean hasParent()
    {
        return this.parent != null;
    }

    public boolean isAmbientOcclusion()
    {
        return this.hasParent() ? this.parent.isAmbientOcclusion() : this.ambientOcclusion;
    }

    public boolean isGui3d()
    {
        return this.gui3d;
    }

    public boolean isResolved()
    {
        return this.parentLocation == null || this.parent != null && this.parent.isResolved();
    }

    public void getParentFromMap(Map<ResourceLocation, ModelBlock> modelMap)
    {
        if (this.parentLocation != null)
        {
            this.parent = modelMap.get(this.parentLocation);
        }
    }

    public boolean isTexturePresent(String textureName)
    {
        return !"missingno".equals(this.resolveTextureName(textureName));
    }

    public String resolveTextureName(String textureName)
    {
        if (!this.startsWithHash(textureName))
        {
            textureName = '#' + textureName;
        }

        return this.resolveTextureName(textureName, new ModelBlock.Bookkeep(this));
    }

    private String resolveTextureName(String textureName, ModelBlock.Bookkeep bookkeep)
    {
        if (this.startsWithHash(textureName))
        {
            if (this == bookkeep.modelExt)
            {
                LOGGER.warn("Unable to resolve texture due to upward reference: " + textureName + " in " + this.name);
                return "missingno";
            }
            else
            {
                String resolvedTexture = this.textures.get(textureName.substring(1));

                if (resolvedTexture == null && this.hasParent())
                {
                    resolvedTexture = this.parent.resolveTextureName(textureName, bookkeep);
                }

                bookkeep.modelExt = this;

                if (resolvedTexture != null && this.startsWithHash(resolvedTexture))
                {
                    resolvedTexture = bookkeep.model.resolveTextureName(resolvedTexture, bookkeep);
                }

                return resolvedTexture != null && !this.startsWithHash(resolvedTexture) ? resolvedTexture : "missingno";
            }
        }
        else
        {
            return textureName;
        }
    }

    private boolean startsWithHash(String hash)
    {
        return hash.charAt(0) == 35;
    }

    public ResourceLocation getParentLocation()
    {
        return this.parentLocation;
    }

    public ModelBlock getRootModel()
    {
        return this.hasParent() ? this.parent.getRootModel() : this;
    }

    public ItemCameraTransforms getAllTransforms()
    {
        ItemTransformVec3f thirdPersonTransform = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON);
        ItemTransformVec3f firstPersonTransform = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON);
        ItemTransformVec3f headTransform = this.getTransform(ItemCameraTransforms.TransformType.HEAD);
        ItemTransformVec3f guiTransform = this.getTransform(ItemCameraTransforms.TransformType.GUI);
        ItemTransformVec3f groundTransform = this.getTransform(ItemCameraTransforms.TransformType.GROUND);
        ItemTransformVec3f fixedTransform = this.getTransform(ItemCameraTransforms.TransformType.FIXED);
        return new ItemCameraTransforms(thirdPersonTransform, firstPersonTransform, headTransform, guiTransform, groundTransform, fixedTransform);
    }

    private ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType type)
    {
        return this.parent != null && !this.cameraTransforms.hasTransform(type) ? this.parent.getTransform(type) : this.cameraTransforms.getTransform(type);
    }

    public static void checkModelHierarchy(Map<ResourceLocation, ModelBlock> modelMap)
    {
        for (ModelBlock modelBlock : modelMap.values())
        {
            try
            {
                ModelBlock parentModel = modelBlock.parent;

                for (ModelBlock ancestorModel = parentModel.parent; parentModel != ancestorModel; ancestorModel = ancestorModel.parent.parent)
                {
                    parentModel = parentModel.parent;
                }

                throw new ModelBlock.LoopException();
            }
            catch (NullPointerException caughtNullPointerException)
            {
                ;
            }
        }
    }

    static final class Bookkeep
    {
        public final ModelBlock model;
        public ModelBlock modelExt;

        private Bookkeep(ModelBlock modelIn)
        {
            this.model = modelIn;
        }
    }

    public static class Deserializer implements JsonDeserializer<ModelBlock>
    {
        public ModelBlock deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            List<BlockPart> elements = this.getModelElements(context, jsonObject);
            String parentName = this.getParent(jsonObject);
            boolean hasNoParent = StringUtils.isEmpty(parentName);
            boolean hasNoElements = elements.isEmpty();

            if (hasNoElements && hasNoParent)
            {
                throw new JsonParseException("BlockModel requires either elements or parent, found neither");
            }
            else if (!hasNoParent && !hasNoElements)
            {
                throw new JsonParseException("BlockModel requires either elements or parent, found both");
            }
            else
            {
                Map<String, String> textures = this.getTextures(jsonObject);
                boolean ambientOcclusion = this.getAmbientOcclusionEnabled(jsonObject);
                ItemCameraTransforms cameraTransforms = ItemCameraTransforms.DEFAULT;

                if (jsonObject.has("display"))
                {
                    JsonObject displayObject = JsonUtils.getJsonObject(jsonObject, "display");
                    cameraTransforms = context.deserialize(displayObject, ItemCameraTransforms.class);
                }

                return hasNoElements ? new ModelBlock(new ResourceLocation(parentName), textures, ambientOcclusion, true, cameraTransforms) : new ModelBlock(elements, textures, ambientOcclusion, true, cameraTransforms);
            }
        }

        private Map<String, String> getTextures(JsonObject jsonObject)
        {
            Map<String, String> textures = Maps.<String, String>newHashMap();

            if (jsonObject.has("textures"))
            {
                JsonObject texturesObject = jsonObject.getAsJsonObject("textures");

                for (Entry<String, JsonElement> entry : texturesObject.entrySet())
                {
                    textures.put(entry.getKey(), ((JsonElement)entry.getValue()).getAsString());
                }
            }

            return textures;
        }

        private String getParent(JsonObject jsonObject)
        {
            return JsonUtils.getString(jsonObject, "parent", "");
        }

        protected boolean getAmbientOcclusionEnabled(JsonObject jsonObject)
        {
            return JsonUtils.getBoolean(jsonObject, "ambientocclusion", true);
        }

        protected List<BlockPart> getModelElements(JsonDeserializationContext context, JsonObject jsonObject)
        {
            List<BlockPart> elements = Lists.<BlockPart>newArrayList();

            if (jsonObject.has("elements"))
            {
                for (JsonElement jsonElement : JsonUtils.getJsonArray(jsonObject, "elements"))
                {
                    elements.add(context.deserialize(jsonElement, BlockPart.class));
                }
            }

            return elements;
        }
    }

    public static class LoopException extends RuntimeException
    {
    }
}
