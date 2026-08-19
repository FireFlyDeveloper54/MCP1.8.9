package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSnowMan;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowMan;
import net.minecraft.entity.monster.EntitySnowman;

public class ModelAdapterSnowman extends ModelAdapter
{
    public ModelAdapterSnowman()
    {
        super(EntitySnowman.class, "snow_golem", 0.5F);
    }

    public ModelBase makeModel()
    {
        return new ModelSnowMan();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelSnowMan))
        {
            return null;
        }
        else
        {
            ModelSnowMan modelSnowMan = (ModelSnowMan)model;
            return modelPart.equals("body") ? modelSnowMan.body : (modelPart.equals("body_bottom") ? modelSnowMan.bottomBody : (modelPart.equals("head") ? modelSnowMan.head : (modelPart.equals("left_hand") ? modelSnowMan.leftHand : (modelPart.equals("right_hand") ? modelSnowMan.rightHand : null))));
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"body", "body_bottom", "head", "right_hand", "left_hand"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderSnowMan renderSnowMan = new RenderSnowMan(renderManager);
        renderSnowMan.mainModel = modelBase;
        renderSnowMan.shadowSize = shadowSize;
        return renderSnowMan;
    }
}
