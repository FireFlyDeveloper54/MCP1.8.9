package net.optifine.entity.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public abstract class ModelAdapterBiped extends ModelAdapter
{
    public ModelAdapterBiped(Class entityClass, String name, float shadowSize)
    {
        super(entityClass, name, shadowSize);
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelBiped))
        {
            return null;
        }
        else
        {
            ModelBiped modelBiped = (ModelBiped)model;
            return modelPart.equals("head") ? modelBiped.bipedHead : (modelPart.equals("headwear") ? modelBiped.bipedHeadwear : (modelPart.equals("body") ? modelBiped.bipedBody : (modelPart.equals("left_arm") ? modelBiped.bipedLeftArm : (modelPart.equals("right_arm") ? modelBiped.bipedRightArm : (modelPart.equals("left_leg") ? modelBiped.bipedLeftLeg : (modelPart.equals("right_leg") ? modelBiped.bipedRightLeg : null))))));
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"head", "headwear", "body", "left_arm", "right_arm", "left_leg", "right_leg"};
    }
}
