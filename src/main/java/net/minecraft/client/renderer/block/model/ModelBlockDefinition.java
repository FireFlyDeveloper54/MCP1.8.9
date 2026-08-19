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
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class ModelBlockDefinition
{
    static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(ModelBlockDefinition.class, new ModelBlockDefinition.Deserializer()).registerTypeAdapter(ModelBlockDefinition.Variant.class, new ModelBlockDefinition.Variant.Deserializer()).create();
    private final Map<String, ModelBlockDefinition.Variants> mapVariants = Maps.<String, ModelBlockDefinition.Variants>newHashMap();

    public static ModelBlockDefinition parseFromReader(Reader reader)
    {
        return GSON.fromJson(reader, ModelBlockDefinition.class);
    }

    public ModelBlockDefinition(Collection<ModelBlockDefinition.Variants> variants)
    {
        for (ModelBlockDefinition.Variants variantList : variants)
        {
            this.mapVariants.put(variantList.name, variantList);
        }
    }

    public ModelBlockDefinition(List<ModelBlockDefinition> definitions)
    {
        for (ModelBlockDefinition modelBlockDefinition : definitions)
        {
            this.mapVariants.putAll(modelBlockDefinition.mapVariants);
        }
    }

    public ModelBlockDefinition.Variants getVariants(String name)
    {
        ModelBlockDefinition.Variants modelblockdefinition$variants = this.mapVariants.get(name);

        if (modelblockdefinition$variants == null)
        {
            throw new ModelBlockDefinition.MissingVariantException();
        }
        else
        {
            return modelblockdefinition$variants;
        }
    }

    public boolean equals(Object objectIn)
    {
        if (this == objectIn)
        {
            return true;
        }
        else if (objectIn instanceof ModelBlockDefinition)
        {
            ModelBlockDefinition modelBlockDefinition = (ModelBlockDefinition)objectIn;
            return this.mapVariants.equals(modelBlockDefinition.mapVariants);
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return this.mapVariants.hashCode();
    }

    public static class Deserializer implements JsonDeserializer<ModelBlockDefinition>
    {
        public ModelBlockDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            List<ModelBlockDefinition.Variants> variants = this.parseVariantsList(context, jsonObject);
            return new ModelBlockDefinition(variants);
        }

        protected List<ModelBlockDefinition.Variants> parseVariantsList(JsonDeserializationContext context, JsonObject object)
        {
            JsonObject variantsObject = JsonUtils.getJsonObject(object, "variants");
            List<ModelBlockDefinition.Variants> variants = Lists.<ModelBlockDefinition.Variants>newArrayList();

            for (Entry<String, JsonElement> entry : variantsObject.entrySet())
            {
                variants.add(this.parseVariants(context, entry));
            }

            return variants;
        }

        protected ModelBlockDefinition.Variants parseVariants(JsonDeserializationContext context, Entry<String, JsonElement> entry)
        {
            String variantName = (String)entry.getKey();
            List<ModelBlockDefinition.Variant> variants = Lists.<ModelBlockDefinition.Variant>newArrayList();
            JsonElement jsonElement = (JsonElement)entry.getValue();

            if (jsonElement.isJsonArray())
            {
                for (JsonElement variantElement : jsonElement.getAsJsonArray())
                {
                    variants.add(context.deserialize(variantElement, ModelBlockDefinition.Variant.class));
                }
            }
            else
            {
                variants.add(context.deserialize(jsonElement, ModelBlockDefinition.Variant.class));
            }

            return new ModelBlockDefinition.Variants(variantName, variants);
        }
    }

    public class MissingVariantException extends RuntimeException
    {
    }

    public static class Variant
    {
        private final ResourceLocation modelLocation;
        private final ModelRotation modelRotation;
        private final boolean uvLock;
        private final int weight;

        public Variant(ResourceLocation modelLocationIn, ModelRotation modelRotationIn, boolean uvLockIn, int weightIn)
        {
            this.modelLocation = modelLocationIn;
            this.modelRotation = modelRotationIn;
            this.uvLock = uvLockIn;
            this.weight = weightIn;
        }

        public ResourceLocation getModelLocation()
        {
            return this.modelLocation;
        }

        public ModelRotation getRotation()
        {
            return this.modelRotation;
        }

        public boolean isUvLocked()
        {
            return this.uvLock;
        }

        public int getWeight()
        {
            return this.weight;
        }

        public boolean equals(Object objectIn)
        {
            if (this == objectIn)
            {
                return true;
            }
            else if (!(objectIn instanceof ModelBlockDefinition.Variant))
            {
                return false;
            }
            else
            {
                ModelBlockDefinition.Variant otherVariant = (ModelBlockDefinition.Variant)objectIn;
                return this.modelLocation.equals(otherVariant.modelLocation) && this.modelRotation == otherVariant.modelRotation && this.uvLock == otherVariant.uvLock;
            }
        }

        public int hashCode()
        {
            int hash = this.modelLocation.hashCode();
            hash = 31 * hash + (this.modelRotation != null ? this.modelRotation.hashCode() : 0);
            hash = 31 * hash + (this.uvLock ? 1 : 0);
            return hash;
        }

        public static class Deserializer implements JsonDeserializer<ModelBlockDefinition.Variant>
        {
            public ModelBlockDefinition.Variant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
            {
                JsonObject jsonObject = json.getAsJsonObject();
                String model = this.parseModel(jsonObject);
                ModelRotation modelRotation = this.parseRotation(jsonObject);
                boolean uvLock = this.parseUvLock(jsonObject);
                int weight = this.parseWeight(jsonObject);
                return new ModelBlockDefinition.Variant(this.makeModelLocation(model), modelRotation, uvLock, weight);
            }

            private ResourceLocation makeModelLocation(String model)
            {
                ResourceLocation resourceLocation = new ResourceLocation(model);
                resourceLocation = new ResourceLocation(resourceLocation.getResourceDomain(), "block/" + resourceLocation.getResourcePath());
                return resourceLocation;
            }

            private boolean parseUvLock(JsonObject object)
            {
                return JsonUtils.getBoolean(object, "uvlock", false);
            }

            protected ModelRotation parseRotation(JsonObject object)
            {
                int rotationX = JsonUtils.getInt(object, "x", 0);
                int rotationY = JsonUtils.getInt(object, "y", 0);
                ModelRotation modelRotation = ModelRotation.getModelRotation(rotationX, rotationY);

                if (modelRotation == null)
                {
                    throw new JsonParseException("Invalid BlockModelRotation x: " + rotationX + ", y: " + rotationY);
                }
                else
                {
                    return modelRotation;
                }
            }

            protected String parseModel(JsonObject object)
            {
                return JsonUtils.getString(object, "model");
            }

            protected int parseWeight(JsonObject object)
            {
                return JsonUtils.getInt(object, "weight", 1);
            }
        }
    }

    public static class Variants
    {
        private final String name;
        private final List<ModelBlockDefinition.Variant> listVariants;

        public Variants(String nameIn, List<ModelBlockDefinition.Variant> listVariantsIn)
        {
            this.name = nameIn;
            this.listVariants = listVariantsIn;
        }

        public List<ModelBlockDefinition.Variant> getVariants()
        {
            return this.listVariants;
        }

        public boolean equals(Object objectIn)
        {
            if (this == objectIn)
            {
                return true;
            }
            else if (!(objectIn instanceof ModelBlockDefinition.Variants))
            {
                return false;
            }
            else
            {
                ModelBlockDefinition.Variants modelblockdefinition$variants = (ModelBlockDefinition.Variants)objectIn;
                return !this.name.equals(modelblockdefinition$variants.name) ? false : this.listVariants.equals(modelblockdefinition$variants.listVariants);
            }
        }

        public int hashCode()
        {
            int hash = this.name.hashCode();
            hash = 31 * hash + this.listVariants.hashCode();
            return hash;
        }
    }
}
