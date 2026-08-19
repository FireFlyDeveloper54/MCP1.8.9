package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBlaze;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderBlaze;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.src.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterBlaze extends ModelAdapter
{
    public ModelAdapterBlaze()
    {
        super(EntityBlaze.class, "blaze", 0.5F);
    }

    public ModelBase makeModel()
    {
        return new ModelBlaze();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelBlaze))
        {
            return null;
        }
        else
        {
            ModelBlaze modelBlaze = (ModelBlaze)model;

            if (modelPart.equals("head"))
            {
                return (ModelRenderer)Reflector.getFieldValue(modelBlaze, Reflector.ModelBlaze_blazeHead);
            }
            else
            {
                String stickPrefix = "stick";

                if (modelPart.startsWith(stickPrefix))
                {
                    ModelRenderer[] stickRenderers = (ModelRenderer[])((ModelRenderer[])Reflector.getFieldValue(modelBlaze, Reflector.ModelBlaze_blazeSticks));

                    if (stickRenderers == null)
                    {
                        return null;
                    }
                    else
                    {
                        String stickIndexText = modelPart.substring(stickPrefix.length());
                        int stickIndex = Config.parseInt(stickIndexText, -1);
                        --stickIndex;
                        return stickIndex >= 0 && stickIndex < stickRenderers.length ? stickRenderers[stickIndex] : null;
                    }
                }
                else
                {
                    return null;
                }
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"head", "stick1", "stick2", "stick3", "stick4", "stick5", "stick6", "stick7", "stick8", "stick9", "stick10", "stick11", "stick12"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderBlaze renderBlaze = new RenderBlaze(renderManager);
        renderBlaze.mainModel = modelBase;
        renderBlaze.shadowSize = shadowSize;
        return renderBlaze;
    }
}
