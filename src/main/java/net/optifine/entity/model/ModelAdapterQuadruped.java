package net.optifine.entity.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.model.ModelRenderer;

public abstract class ModelAdapterQuadruped extends ModelAdapter
{
    public ModelAdapterQuadruped(Class entityClass, String name, float shadowSize)
    {
        super(entityClass, name, shadowSize);
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelQuadruped))
        {
            return null;
        }
        else
        {
            ModelQuadruped modelQuadruped = (ModelQuadruped)model;
            return modelPart.equals("head") ? modelQuadruped.head : (modelPart.equals("body") ? modelQuadruped.body : (modelPart.equals("leg1") ? modelQuadruped.leg1 : (modelPart.equals("leg2") ? modelQuadruped.leg2 : (modelPart.equals("leg3") ? modelQuadruped.leg3 : (modelPart.equals("leg4") ? modelQuadruped.leg4 : null)))));
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"head", "body", "leg1", "leg2", "leg3", "leg4"};
    }
}
