package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelEnderMite;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderEndermite;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.src.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterEndermite extends ModelAdapter
{
    public ModelAdapterEndermite()
    {
        super(EntityEndermite.class, "endermite", 0.3F);
    }

    public ModelBase makeModel()
    {
        return new ModelEnderMite();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelEnderMite))
        {
            return null;
        }
        else
        {
            ModelEnderMite modelEndermite = (ModelEnderMite)model;
            String bodyPrefix = "body";

            if (modelPart.startsWith(bodyPrefix))
            {
                ModelRenderer[] bodyRenderers = (ModelRenderer[])((ModelRenderer[])Reflector.getFieldValue(modelEndermite, Reflector.ModelEnderMite_bodyParts));

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
                return null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"body1", "body2", "body3", "body4"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderEndermite renderEndermite = new RenderEndermite(renderManager);
        renderEndermite.mainModel = modelBase;
        renderEndermite.shadowSize = shadowSize;
        return renderEndermite;
    }
}
