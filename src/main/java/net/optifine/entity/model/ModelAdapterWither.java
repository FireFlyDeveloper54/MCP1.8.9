package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelWither;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderWither;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.src.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterWither extends ModelAdapter
{
    public ModelAdapterWither()
    {
        super(EntityWither.class, "wither", 0.5F);
    }

    public ModelBase makeModel()
    {
        return new ModelWither(0.0F);
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelWither))
        {
            return null;
        }
        else
        {
            ModelWither modelWither = (ModelWither)model;
            String bodyPrefix = "body";

            if (modelPart.startsWith(bodyPrefix))
            {
                ModelRenderer[] bodyRenderers = (ModelRenderer[])((ModelRenderer[])Reflector.getFieldValue(modelWither, Reflector.ModelWither_bodyParts));

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
                String headPrefix = "head";

                if (modelPart.startsWith(headPrefix))
                {
                    ModelRenderer[] headRenderers = (ModelRenderer[])((ModelRenderer[])Reflector.getFieldValue(modelWither, Reflector.ModelWither_heads));

                    if (headRenderers == null)
                    {
                        return null;
                    }
                    else
                    {
                        String headIndexText = modelPart.substring(headPrefix.length());
                        int headIndex = Config.parseInt(headIndexText, -1);
                        --headIndex;
                        return headIndex >= 0 && headIndex < headRenderers.length ? headRenderers[headIndex] : null;
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
        return new String[] {"body1", "body2", "body3", "head1", "head2", "head3"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderWither renderWither = new RenderWither(renderManager);
        renderWither.mainModel = modelBase;
        renderWither.shadowSize = shadowSize;
        return renderWither;
    }
}
