package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelGhast;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderGhast;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.src.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterGhast extends ModelAdapter
{
    public ModelAdapterGhast()
    {
        super(EntityGhast.class, "ghast", 0.5F);
    }

    public ModelBase makeModel()
    {
        return new ModelGhast();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelGhast))
        {
            return null;
        }
        else
        {
            ModelGhast modelGhast = (ModelGhast)model;

            if (modelPart.equals("body"))
            {
                return (ModelRenderer)Reflector.getFieldValue(modelGhast, Reflector.ModelGhast_body);
            }
            else
            {
                String tentaclePrefix = "tentacle";

                if (modelPart.startsWith(tentaclePrefix))
                {
                    ModelRenderer[] tentacleRenderers = (ModelRenderer[])((ModelRenderer[])Reflector.getFieldValue(modelGhast, Reflector.ModelGhast_tentacles));

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
        return new String[] {"body", "tentacle1", "tentacle2", "tentacle3", "tentacle4", "tentacle5", "tentacle6", "tentacle7", "tentacle8", "tentacle9"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderGhast renderGhast = new RenderGhast(renderManager);
        renderGhast.mainModel = modelBase;
        renderGhast.shadowSize = shadowSize;
        return renderGhast;
    }
}
