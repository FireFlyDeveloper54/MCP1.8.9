package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderIronGolem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityIronGolem;

public class ModelAdapterIronGolem extends ModelAdapter
{
    public ModelAdapterIronGolem()
    {
        super(EntityIronGolem.class, "iron_golem", 0.5F);
    }

    public ModelBase makeModel()
    {
        return new ModelIronGolem();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelIronGolem))
        {
            return null;
        }
        else
        {
            ModelIronGolem modelIronGolem = (ModelIronGolem)model;
            return modelPart.equals("head") ? modelIronGolem.ironGolemHead : (modelPart.equals("body") ? modelIronGolem.ironGolemBody : (modelPart.equals("left_arm") ? modelIronGolem.ironGolemLeftArm : (modelPart.equals("right_arm") ? modelIronGolem.ironGolemRightArm : (modelPart.equals("left_leg") ? modelIronGolem.ironGolemLeftLeg : (modelPart.equals("right_leg") ? modelIronGolem.ironGolemRightLeg : null)))));
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"head", "body", "right_arm", "left_arm", "left_leg", "right_leg"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderIronGolem renderIronGolem = new RenderIronGolem(renderManager);
        renderIronGolem.mainModel = modelBase;
        renderIronGolem.shadowSize = shadowSize;
        return renderIronGolem;
    }
}
