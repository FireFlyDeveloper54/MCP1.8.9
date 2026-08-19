package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSilverfish;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSilverfish;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.src.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterSilverfish extends ModelAdapter
{
    public ModelAdapterSilverfish()
    {
        super(EntitySilverfish.class, "silverfish", 0.3F);
    }

    public ModelBase makeModel()
    {
        return new ModelSilverfish();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelSilverfish))
        {
            return null;
        }
        else
        {
            ModelSilverfish modelSilverfish = (ModelSilverfish)model;
            String bodyPrefix = "body";

            if (modelPart.startsWith(bodyPrefix))
            {
                ModelRenderer[] bodyRenderers = (ModelRenderer[])((ModelRenderer[])Reflector.getFieldValue(modelSilverfish, Reflector.ModelSilverfish_bodyParts));

                if (bodyRenderers == null)
                {
                    return null;
                }
                else
                {
                    String bodyIndexText = modelPart.substring(bodyPrefix.length());
                    int bodyIndex = Config.parseInt(bodyIndexText, -1);
                    --bodyIndex;
                    return bodyIndex >= 0 && bodyIndex < bodyRenderers.length ? bodyRenderers[bodyIndex] : null;
                }
            }
            else
            {
                String wingPrefix = "wing";

                if (modelPart.startsWith(wingPrefix))
                {
                    ModelRenderer[] wingRenderers = (ModelRenderer[])((ModelRenderer[])Reflector.getFieldValue(modelSilverfish, Reflector.ModelSilverfish_wingParts));

                    if (wingRenderers == null)
                    {
                        return null;
                    }
                    else
                    {
                        String wingIndexText = modelPart.substring(wingPrefix.length());
                        int wingIndex = Config.parseInt(wingIndexText, -1);
                        --wingIndex;
                        return wingIndex >= 0 && wingIndex < wingRenderers.length ? wingRenderers[wingIndex] : null;
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
        return new String[] {"body1", "body2", "body3", "body4", "body5", "body6", "body7", "wing1", "wing2", "wing3"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderSilverfish renderSilverfish = new RenderSilverfish(renderManager);
        renderSilverfish.mainModel = modelBase;
        renderSilverfish.shadowSize = shadowSize;
        return renderSilverfish;
    }
}
