package net.optifine.entity.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public abstract class ModelAdapter
{
    private Class entityClass;
    private String name;
    private float shadowSize;
    private String[] aliases;

    public ModelAdapter(Class entityClass, String name, float shadowSize)
    {
        this.entityClass = entityClass;
        this.name = name;
        this.shadowSize = shadowSize;
    }

    public ModelAdapter(Class entityClass, String name, float shadowSize, String[] aliases)
    {
        this.entityClass = entityClass;
        this.name = name;
        this.shadowSize = shadowSize;
        this.aliases = aliases;
    }

    public Class getEntityClass()
    {
        return this.entityClass;
    }

    public String getName()
    {
        return this.name;
    }

    public String[] getAliases()
    {
        return this.aliases;
    }

    public float getShadowSize()
    {
        return this.shadowSize;
    }

    public abstract ModelBase makeModel();

    public abstract ModelRenderer getModelRenderer(ModelBase model, String modelPart);

    public abstract String[] getModelRendererNames();

    public abstract IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize);

    public ModelRenderer[] getModelRenderers(ModelBase model)
    {
        String[] modelRendererNames = this.getModelRendererNames();
        List<ModelRenderer> modelRenderers = new ArrayList();

        for (int rendererIndex = 0; rendererIndex < modelRendererNames.length; ++rendererIndex)
        {
            String modelRendererName = modelRendererNames[rendererIndex];
            ModelRenderer modelRenderer = this.getModelRenderer(model, modelRendererName);

            if (modelRenderer != null)
            {
                modelRenderers.add(modelRenderer);
            }
        }

        ModelRenderer[] modelRendererArray = (ModelRenderer[])((ModelRenderer[])modelRenderers.toArray(new ModelRenderer[modelRenderers.size()]));
        return modelRendererArray;
    }
}
