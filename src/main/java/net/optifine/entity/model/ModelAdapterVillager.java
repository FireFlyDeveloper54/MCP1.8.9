package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderVillager;
import net.minecraft.entity.passive.EntityVillager;

public class ModelAdapterVillager extends ModelAdapter
{
    public ModelAdapterVillager()
    {
        super(EntityVillager.class, "villager", 0.5F);
    }

    public ModelBase makeModel()
    {
        return new ModelVillager(0.0F);
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelVillager))
        {
            return null;
        }
        else
        {
            ModelVillager modelVillager = (ModelVillager)model;
            return modelPart.equals("head") ? modelVillager.villagerHead : (modelPart.equals("body") ? modelVillager.villagerBody : (modelPart.equals("arms") ? modelVillager.villagerArms : (modelPart.equals("left_leg") ? modelVillager.leftVillagerLeg : (modelPart.equals("right_leg") ? modelVillager.rightVillagerLeg : (modelPart.equals("nose") ? modelVillager.villagerNose : null)))));
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"head", "body", "arms", "right_leg", "left_leg", "nose"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderVillager renderVillager = new RenderVillager(renderManager);
        renderVillager.mainModel = modelBase;
        renderVillager.shadowSize = shadowSize;
        return renderVillager;
    }
}
