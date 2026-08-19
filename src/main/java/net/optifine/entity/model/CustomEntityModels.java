package net.optifine.entity.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.entity.model.anim.ModelResolver;
import net.optifine.entity.model.anim.ModelUpdater;

public class CustomEntityModels
{
    private static boolean active = false;
    private static Map<Class, Render> originalEntityRenderMap = null;
    private static Map<Class, TileEntitySpecialRenderer> originalTileEntityRenderMap = null;

    public static void update()
    {
        Map<Class, Render> entityRenderMap = getEntityRenderMap();
        Map<Class, TileEntitySpecialRenderer> tileEntityRenderMap = getTileEntityRenderMap();

        if (entityRenderMap == null)
        {
            Config.warn("Entity render map not found, custom entity models are DISABLED.");
        }
        else if (tileEntityRenderMap == null)
        {
            Config.warn("Tile entity render map not found, custom entity models are DISABLED.");
        }
        else
        {
            active = false;
            entityRenderMap.clear();
            tileEntityRenderMap.clear();
            entityRenderMap.putAll(originalEntityRenderMap);
            tileEntityRenderMap.putAll(originalTileEntityRenderMap);

            if (Config.isCustomEntityModels())
            {
                ResourceLocation[] modelLocations = getModelLocations();

                for (int modelIndex = 0; modelIndex < modelLocations.length; ++modelIndex)
                {
                    ResourceLocation modelLocation = modelLocations[modelIndex];
                    Config.dbg("CustomEntityModel: " + modelLocation.getResourcePath());
                    IEntityRenderer entityRenderer = parseEntityRender(modelLocation);

                    if (entityRenderer != null)
                    {
                        Class entityClass = entityRenderer.getEntityClass();

                        if (entityClass != null)
                        {
                            if (entityRenderer instanceof Render)
                            {
                                entityRenderMap.put(entityClass, (Render)entityRenderer);
                            }
                            else if (entityRenderer instanceof TileEntitySpecialRenderer)
                            {
                                tileEntityRenderMap.put(entityClass, (TileEntitySpecialRenderer)entityRenderer);
                            }
                            else
                            {
                                Config.warn("Unknown renderer type: " + entityRenderer.getClass().getName());
                            }

                            active = true;
                        }
                    }
                }
            }
        }
    }

    private static Map<Class, Render> getEntityRenderMap()
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        Map<Class, Render> entityRenderMap = renderManager.getEntityRenderMap();

        if (entityRenderMap == null)
        {
            return null;
        }
        else
        {
            if (originalEntityRenderMap == null)
            {
                originalEntityRenderMap = new HashMap(entityRenderMap);
            }

            return entityRenderMap;
        }
    }

    private static Map<Class, TileEntitySpecialRenderer> getTileEntityRenderMap()
    {
        Map<Class, TileEntitySpecialRenderer> tileEntityRenderMap = TileEntityRendererDispatcher.instance.mapSpecialRenderers;

        if (originalTileEntityRenderMap == null)
        {
            originalTileEntityRenderMap = new HashMap<>(tileEntityRenderMap);
        }

        return tileEntityRenderMap;
    }

    private static ResourceLocation[] getModelLocations()
    {
        String modelPath = "optifine/cem/";
        String modelSuffix = ".jem";
        List<ResourceLocation> modelLocations = new ArrayList();
        String[] modelNames = CustomModelRegistry.getModelNames();

        for (int i = 0; i < modelNames.length; ++i)
        {
            String modelName = modelNames[i];
            String modelLocationPath = modelPath + modelName + modelSuffix;
            ResourceLocation modelLocation = new ResourceLocation(modelLocationPath);

            if (Config.hasResource(modelLocation))
            {
                modelLocations.add(modelLocation);
            }
        }

        ResourceLocation[] modelLocationArray = (ResourceLocation[])((ResourceLocation[])modelLocations.toArray(new ResourceLocation[modelLocations.size()]));
        return modelLocationArray;
    }

    private static IEntityRenderer parseEntityRender(ResourceLocation location)
    {
        try
        {
            JsonObject jsonObject = CustomEntityModelParser.loadJson(location);
            IEntityRenderer entityRenderer = parseEntityRender(jsonObject, location.getResourcePath());
            return entityRenderer;
        }
        catch (IOException ioException)
        {
            Config.error("" + ioException.getClass().getName() + ": " + ioException.getMessage());
            return null;
        }
        catch (JsonParseException jsonParseException)
        {
            Config.error("" + jsonParseException.getClass().getName() + ": " + jsonParseException.getMessage());
            return null;
        }
        catch (Exception exception)
        {
            net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            return null;
        }
    }

    private static IEntityRenderer parseEntityRender(JsonObject obj, String path)
    {
        CustomEntityRenderer customEntityRenderer = CustomEntityModelParser.parseEntityRender(obj, path);
        String entityName = customEntityRenderer.getName();
        ModelAdapter modelAdapter = CustomModelRegistry.getModelAdapter(entityName);
        checkNull(modelAdapter, "Entity not found: " + entityName);
        Class entityClass = modelAdapter.getEntityClass();
        checkNull(entityClass, "Entity class not found: " + entityName);
        IEntityRenderer entityRenderer = makeEntityRender(modelAdapter, customEntityRenderer);

        if (entityRenderer == null)
        {
            return null;
        }
        else
        {
            entityRenderer.setEntityClass(entityClass);
            return entityRenderer;
        }
    }

    private static IEntityRenderer makeEntityRender(ModelAdapter modelAdapter, CustomEntityRenderer cer)
    {
        ResourceLocation textureLocation = cer.getTextureLocation();
        CustomModelRenderer[] customModelRenderers = cer.getCustomModelRenderers();
        float shadowSize = cer.getShadowSize();

        if (shadowSize < 0.0F)
        {
            shadowSize = modelAdapter.getShadowSize();
        }

        ModelBase modelBase = modelAdapter.makeModel();

        if (modelBase == null)
        {
            return null;
        }
        else
        {
            ModelResolver modelResolver = new ModelResolver(modelAdapter, modelBase, customModelRenderers);

            if (!modifyModel(modelAdapter, modelBase, customModelRenderers, modelResolver))
            {
                return null;
            }
            else
            {
                IEntityRenderer entityRenderer = modelAdapter.makeEntityRender(modelBase, shadowSize);

                if (entityRenderer == null)
                {
                    throw new JsonParseException("Entity renderer is null, model: " + modelAdapter.getName() + ", adapter: " + modelAdapter.getClass().getName());
                }
                else
                {
                    if (textureLocation != null)
                    {
                        entityRenderer.setLocationTextureCustom(textureLocation);
                    }

                    return entityRenderer;
                }
            }
        }
    }

    private static boolean modifyModel(ModelAdapter modelAdapter, ModelBase model, CustomModelRenderer[] modelRenderers, ModelResolver modelResolver)
    {
        for (int modelIndex = 0; modelIndex < modelRenderers.length; ++modelIndex)
        {
            CustomModelRenderer customModelRenderer = modelRenderers[modelIndex];

            if (!modifyModel(modelAdapter, model, customModelRenderer, modelResolver))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean modifyModel(ModelAdapter modelAdapter, ModelBase model, CustomModelRenderer customModelRenderer, ModelResolver modelResolver)
    {
        String modelPart = customModelRenderer.getModelPart();
        ModelRenderer modelRenderer = modelAdapter.getModelRenderer(model, modelPart);

        if (modelRenderer == null)
        {
            Config.warn("Model part not found: " + modelPart + ", model: " + model);
            return false;
        }
        else
        {
            if (!customModelRenderer.isAttach())
            {
                if (modelRenderer.cubeList != null)
                {
                    modelRenderer.cubeList.clear();
                }

                if (modelRenderer.spriteList != null)
                {
                    modelRenderer.spriteList.clear();
                }

                if (modelRenderer.childModels != null)
                {
                    ModelRenderer[] adapterModelRenderers = modelAdapter.getModelRenderers(model);
                    Set<ModelRenderer> adapterModelRendererSet = Collections.<ModelRenderer>newSetFromMap(new IdentityHashMap());
                    adapterModelRendererSet.addAll(Arrays.<ModelRenderer>asList(adapterModelRenderers));
                    List<ModelRenderer> childModels = modelRenderer.childModels;
                    Iterator childIterator = childModels.iterator();

                    while (childIterator.hasNext())
                    {
                        ModelRenderer childModelRenderer = (ModelRenderer)childIterator.next();

                        if (!adapterModelRendererSet.contains(childModelRenderer))
                        {
                            childIterator.remove();
                        }
                    }
                }
            }

            modelRenderer.addChild(customModelRenderer.getModelRenderer());
            ModelUpdater modelUpdater = customModelRenderer.getModelUpdater();

            if (modelUpdater != null)
            {
                modelResolver.setThisModelRenderer(customModelRenderer.getModelRenderer());
                modelResolver.setPartModelRenderer(modelRenderer);

                if (!modelUpdater.initialize(modelResolver))
                {
                    return false;
                }

                customModelRenderer.getModelRenderer().setModelUpdater(modelUpdater);
            }

            return true;
        }
    }

    private static void checkNull(Object obj, String msg)
    {
        if (obj == null)
        {
            throw new JsonParseException(msg);
        }
    }

    public static boolean isActive()
    {
        return active;
    }
}
