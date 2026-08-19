package net.optifine.entity.model.anim;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.optifine.entity.model.CustomModelRenderer;
import net.optifine.entity.model.ModelAdapter;
import net.optifine.expr.IExpression;

public class ModelResolver implements IModelResolver
{
    private ModelAdapter modelAdapter;
    private ModelBase model;
    private CustomModelRenderer[] customModelRenderers;
    private ModelRenderer thisModelRenderer;
    private ModelRenderer partModelRenderer;
    private IRenderResolver renderResolver;

    public ModelResolver(ModelAdapter modelAdapter, ModelBase model, CustomModelRenderer[] customModelRenderers)
    {
        this.modelAdapter = modelAdapter;
        this.model = model;
        this.customModelRenderers = customModelRenderers;
        Class oclass = modelAdapter.getEntityClass();

        if (TileEntity.class.isAssignableFrom(oclass))
        {
            this.renderResolver = new RenderResolverTileEntity();
        }
        else
        {
            this.renderResolver = new RenderResolverEntity();
        }
    }

    public IExpression getExpression(String name)
    {
        IExpression modelVariable = this.getModelVariable(name);

        if (modelVariable != null)
        {
            return modelVariable;
        }
        else
        {
            IExpression renderParameter = this.renderResolver.getParameter(name);
            return renderParameter != null ? renderParameter : null;
        }
    }

    public ModelRenderer getModelRenderer(String name)
    {
        if (name == null)
        {
            return null;
        }
        else if (name.indexOf(":") >= 0)
        {
            String[] pathParts = Config.tokenize(name, ":");
            ModelRenderer pathRenderer = this.getModelRenderer(pathParts[0]);

            for (int partIndex = 1; partIndex < pathParts.length; ++partIndex)
            {
                String childName = pathParts[partIndex];
                ModelRenderer childRenderer = pathRenderer.getChildDeep(childName);

                if (childRenderer == null)
                {
                    return null;
                }

                pathRenderer = childRenderer;
            }

            return pathRenderer;
        }
        else if (this.thisModelRenderer != null && name.equals("this"))
        {
            return this.thisModelRenderer;
        }
        else if (this.partModelRenderer != null && name.equals("part"))
        {
            return this.partModelRenderer;
        }
        else
        {
            ModelRenderer modelRenderer = this.modelAdapter.getModelRenderer(this.model, name);

            if (modelRenderer != null)
            {
                return modelRenderer;
            }
            else
            {
                for (int rendererIndex = 0; rendererIndex < this.customModelRenderers.length; ++rendererIndex)
                {
                    CustomModelRenderer customModelRenderer = this.customModelRenderers[rendererIndex];
                    ModelRenderer customRenderer = customModelRenderer.getModelRenderer();

                    if (name.equals(customRenderer.getId()))
                    {
                        return customRenderer;
                    }

                    ModelRenderer childRenderer = customRenderer.getChildDeep(name);

                    if (childRenderer != null)
                    {
                        return childRenderer;
                    }
                }

                return null;
            }
        }
    }

    public ModelVariableFloat getModelVariable(String name)
    {
        String[] nameParts = Config.tokenize(name, ".");

        if (nameParts.length != 2)
        {
            return null;
        }
        else
        {
            String modelPartName = nameParts[0];
            String variableName = nameParts[1];
            ModelRenderer modelRenderer = this.getModelRenderer(modelPartName);

            if (modelRenderer == null)
            {
                return null;
            }
            else
            {
                ModelVariableType variableType = ModelVariableType.parse(variableName);
                return variableType == null ? null : new ModelVariableFloat(name, modelRenderer, variableType);
            }
        }
    }

    public void setPartModelRenderer(ModelRenderer partModelRenderer)
    {
        this.partModelRenderer = partModelRenderer;
    }

    public void setThisModelRenderer(ModelRenderer thisModelRenderer)
    {
        this.thisModelRenderer = thisModelRenderer;
    }
}
