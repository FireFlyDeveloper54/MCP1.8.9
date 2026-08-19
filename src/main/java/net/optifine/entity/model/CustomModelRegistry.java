package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.src.Config;

public class CustomModelRegistry
{
    private static Map<String, ModelAdapter> mapModelAdapters = makeMapModelAdapters();

    private static Map<String, ModelAdapter> makeMapModelAdapters()
    {
        Map<String, ModelAdapter> modelAdapters = new LinkedHashMap();
        addModelAdapter(modelAdapters, new ModelAdapterArmorStand());
        addModelAdapter(modelAdapters, new ModelAdapterBat());
        addModelAdapter(modelAdapters, new ModelAdapterBlaze());
        addModelAdapter(modelAdapters, new ModelAdapterBoat());
        addModelAdapter(modelAdapters, new ModelAdapterCaveSpider());
        addModelAdapter(modelAdapters, new ModelAdapterChicken());
        addModelAdapter(modelAdapters, new ModelAdapterCow());
        addModelAdapter(modelAdapters, new ModelAdapterCreeper());
        addModelAdapter(modelAdapters, new ModelAdapterDragon());
        addModelAdapter(modelAdapters, new ModelAdapterEnderCrystal());
        addModelAdapter(modelAdapters, new ModelAdapterEnderman());
        addModelAdapter(modelAdapters, new ModelAdapterEndermite());
        addModelAdapter(modelAdapters, new ModelAdapterGhast());
        addModelAdapter(modelAdapters, new ModelAdapterGuardian());
        addModelAdapter(modelAdapters, new ModelAdapterHorse());
        addModelAdapter(modelAdapters, new ModelAdapterIronGolem());
        addModelAdapter(modelAdapters, new ModelAdapterLeadKnot());
        addModelAdapter(modelAdapters, new ModelAdapterMagmaCube());
        addModelAdapter(modelAdapters, new ModelAdapterMinecart());
        addModelAdapter(modelAdapters, new ModelAdapterMinecartTnt());
        addModelAdapter(modelAdapters, new ModelAdapterMinecartMobSpawner());
        addModelAdapter(modelAdapters, new ModelAdapterMooshroom());
        addModelAdapter(modelAdapters, new ModelAdapterOcelot());
        addModelAdapter(modelAdapters, new ModelAdapterPig());
        addModelAdapter(modelAdapters, new ModelAdapterPigZombie());
        addModelAdapter(modelAdapters, new ModelAdapterRabbit());
        addModelAdapter(modelAdapters, new ModelAdapterSheep());
        addModelAdapter(modelAdapters, new ModelAdapterSilverfish());
        addModelAdapter(modelAdapters, new ModelAdapterSkeleton());
        addModelAdapter(modelAdapters, new ModelAdapterSlime());
        addModelAdapter(modelAdapters, new ModelAdapterSnowman());
        addModelAdapter(modelAdapters, new ModelAdapterSpider());
        addModelAdapter(modelAdapters, new ModelAdapterSquid());
        addModelAdapter(modelAdapters, new ModelAdapterVillager());
        addModelAdapter(modelAdapters, new ModelAdapterWitch());
        addModelAdapter(modelAdapters, new ModelAdapterWither());
        addModelAdapter(modelAdapters, new ModelAdapterWitherSkull());
        addModelAdapter(modelAdapters, new ModelAdapterWolf());
        addModelAdapter(modelAdapters, new ModelAdapterZombie());
        addModelAdapter(modelAdapters, new ModelAdapterSheepWool());
        addModelAdapter(modelAdapters, new ModelAdapterBanner());
        addModelAdapter(modelAdapters, new ModelAdapterBook());
        addModelAdapter(modelAdapters, new ModelAdapterChest());
        addModelAdapter(modelAdapters, new ModelAdapterChestLarge());
        addModelAdapter(modelAdapters, new ModelAdapterEnderChest());
        addModelAdapter(modelAdapters, new ModelAdapterHeadHumanoid());
        addModelAdapter(modelAdapters, new ModelAdapterHeadSkeleton());
        addModelAdapter(modelAdapters, new ModelAdapterSign());
        return modelAdapters;
    }

    private static void addModelAdapter(Map<String, ModelAdapter> modelAdapters, ModelAdapter modelAdapter)
    {
        addModelAdapter(modelAdapters, modelAdapter, modelAdapter.getName());
        String[] aliases = modelAdapter.getAliases();

        if (aliases != null)
        {
            for (int aliasIndex = 0; aliasIndex < aliases.length; ++aliasIndex)
            {
                String alias = aliases[aliasIndex];
                addModelAdapter(modelAdapters, modelAdapter, alias);
            }
        }

        ModelBase modelBase = modelAdapter.makeModel();
        String[] modelRendererNames = modelAdapter.getModelRendererNames();

        for (int rendererIndex = 0; rendererIndex < modelRendererNames.length; ++rendererIndex)
        {
            String modelRendererName = modelRendererNames[rendererIndex];
            ModelRenderer modelRenderer = modelAdapter.getModelRenderer(modelBase, modelRendererName);

            if (modelRenderer == null)
            {
                Config.warn("Model renderer not found, model: " + modelAdapter.getName() + ", name: " + modelRendererName);
            }
        }
    }

    private static void addModelAdapter(Map<String, ModelAdapter> modelAdapters, ModelAdapter modelAdapter, String name)
    {
        if (modelAdapters.containsKey(name))
        {
            Config.warn("Model adapter already registered for id: " + name + ", class: " + modelAdapter.getEntityClass().getName());
        }

        modelAdapters.put(name, modelAdapter);
    }

    public static ModelAdapter getModelAdapter(String name)
    {
        return (ModelAdapter)mapModelAdapters.get(name);
    }

    public static String[] getModelNames()
    {
        Set<String> modelNameSet = mapModelAdapters.keySet();
        String[] modelNames = (String[])((String[])modelNameSet.toArray(new String[modelNameSet.size()]));
        return modelNames;
    }
}
