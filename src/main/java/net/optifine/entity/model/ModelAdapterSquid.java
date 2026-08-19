package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSquid;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSquid;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.src.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterSquid extends ModelAdapter
{
    public ModelAdapterSquid()
    {
        super(EntitySquid.class, "squid", 0.7F);
    }

    public ModelBase makeModel()
    {
        return new ModelSquid();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelSquid))
        {
            return null;
        }
        else
        {
            ModelSquid modelSquid = (ModelSquid)model;

            if (modelPart.equals("body"))
            {
                return (ModelRenderer)Reflector.getFieldValue(modelSquid, Reflector.ModelSquid_body);
            }
            else
            {
                String tentaclePrefix = "tentacle";

                if (modelPart.startsWith(tentaclePrefix))
                {
                    ModelRenderer[] tentacleRenderers = (ModelRenderer[])((ModelRenderer[])Reflector.getFieldValue(modelSquid, Reflector.ModelSquid_tentacles));

                    if (tentacleRenderers == null)
                    {
                        return null;
                    }
                    else
                    {
                        String tentacleIndexText = modelPart.substring(tentaclePrefix.length());
                        int tentacleIndex = Config.parseInt(tentacleIndexText, -1);
                        --tentacleIndex;
                        return tentacleIndex >= 0 && tentacleIndex < tentacleRenderers.length ? tentacleRenderers[tentacleIndex] : null;
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
        return new String[] {"body", "tentacle1", "tentacle2", "tentacle3", "tentacle4", "tentacle5", "tentacle6", "tentacle7", "tentacle8"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderSquid renderSquid = new RenderSquid(renderManager, modelBase, shadowSize);
        return renderSquid;
    }
}
