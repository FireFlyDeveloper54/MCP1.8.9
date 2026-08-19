package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelGuardian;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderGuardian;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.src.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterGuardian extends ModelAdapter
{
    public ModelAdapterGuardian()
    {
        super(EntityGuardian.class, "guardian", 0.5F);
    }

    public ModelBase makeModel()
    {
        return new ModelGuardian();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart)
    {
        if (!(model instanceof ModelGuardian))
        {
            return null;
        }
        else
        {
            ModelGuardian modelGuardian = (ModelGuardian)model;

            if (modelPart.equals("body"))
            {
                return (ModelRenderer)Reflector.getFieldValue(modelGuardian, Reflector.ModelGuardian_body);
            }
            else if (modelPart.equals("eye"))
            {
                return (ModelRenderer)Reflector.getFieldValue(modelGuardian, Reflector.ModelGuardian_eye);
            }
            else
            {
                String spinePrefix = "spine";

                if (modelPart.startsWith(spinePrefix))
                {
                    ModelRenderer[] spineRenderers = (ModelRenderer[])((ModelRenderer[])Reflector.getFieldValue(modelGuardian, Reflector.ModelGuardian_spines));

                    if (spineRenderers == null)
                    {
                        return null;
                    }
                    else
                    {
                        String spineIndexText = modelPart.substring(spinePrefix.length());
                        int spineIndex = Config.parseInt(spineIndexText, -1);
                        --spineIndex;
                        return spineIndex >= 0 && spineIndex < spineRenderers.length ? spineRenderers[spineIndex] : null;
                    }
                }
                else
                {
                    String tailPrefix = "tail";

                    if (modelPart.startsWith(tailPrefix))
                    {
                        ModelRenderer[] tailRenderers = (ModelRenderer[])((ModelRenderer[])Reflector.getFieldValue(modelGuardian, Reflector.ModelGuardian_tail));

                        if (tailRenderers == null)
                        {
                            return null;
                        }
                        else
                        {
                            String tailIndexText = modelPart.substring(tailPrefix.length());
                            int tailIndex = Config.parseInt(tailIndexText, -1);
                            --tailIndex;
                            return tailIndex >= 0 && tailIndex < tailRenderers.length ? tailRenderers[tailIndex] : null;
                        }
                    }
                    else
                    {
                        return null;
                    }
                }
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"body", "eye", "spine1", "spine2", "spine3", "spine4", "spine5", "spine6", "spine7", "spine8", "spine9", "spine10", "spine11", "spine12", "tail1", "tail2", "tail3"};
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize)
    {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderGuardian renderGuardian = new RenderGuardian(renderManager);
        renderGuardian.mainModel = modelBase;
        renderGuardian.shadowSize = shadowSize;
        return renderGuardian;
    }
}
